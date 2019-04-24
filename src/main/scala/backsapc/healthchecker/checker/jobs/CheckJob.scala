package backsapc.healthchecker.checker.jobs
import java.time.OffsetDateTime
import java.util.UUID

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout
import backsapc.healthchecker.checker.contracts.ServiceLocator
import backsapc.healthchecker.checker.dao.{ CheckerJobRepository, CheckerRepository }
import backsapc.healthchecker.checker.domain.{ CheckType, HttpCheckModel, HttpContentCheckModel, PingCheckModel }
import backsapc.healthchecker.checker.jobs.CheckContentActor.DoHttpContentCheck
import backsapc.healthchecker.checker.jobs.CheckHttpActor.DoHttpCheck
import backsapc.healthchecker.checker.jobs.CheckPingActor.DoPingCheck

import scala.concurrent.Future
import scala.concurrent.duration._

object CheckJob {
  def props(repository: CheckerRepository, jobRepository: CheckerJobRepository, serviceLocator: ServiceLocator): Props =
    Props(new CheckJob(repository, jobRepository, serviceLocator))
}

case object StartCheckCycle
case class CheckFailed(checkId: UUID)

class CheckJob(checkRepo: CheckerRepository, jobRepo: CheckerJobRepository, serviceLocator: ServiceLocator)
    extends Actor
    with ActorLogging {

  import context.dispatcher
  implicit val timeout: Timeout = Timeout(5.seconds)

  val checkHttp: ActorRef = context.actorOf(CheckHttpActor.props(serviceLocator.httpChecker), "http-check-actor")
  val checkContent: ActorRef =
    context.actorOf(CheckContentActor.props(serviceLocator.httpContentChecker), "content-check-actor")
  val checkPing: ActorRef = context.actorOf(CheckPingActor.props(serviceLocator.pingChecker), "ping-check-actor")

  override def receive = {
    case StartCheckCycle =>
      println("starter checks")

      val currentTasks = for {
        _          <- lookForNewCheckEvents()
        notStarted <- jobRepo.getNotStartedCheckEvents(100)
        _          <- jobRepo.updateCheckEventsAsStarted(notStarted.map(_.id))
        checks     <- checkRepo.getByIds(notStarted.map(_.checkId))
        tasks = notStarted.map(event => (event, checks.find(_.id == event.checkId)))
      } yield tasks

      val checkRound = currentTasks.flatMap(
        tasks => {
          val sequence = tasks
            .filter(_._2.isDefined)
            .map(task => {
              val event = task._1
              val check = task._2.get
              val result = check.checkType match {
                case CheckType.Http =>
                  checkHttp ? DoHttpCheck(HttpCheckModel(check.id, check.userId, check.url.get))

                case CheckType.HttpWithContent =>
                  checkContent ? DoHttpContentCheck(
                    HttpContentCheckModel(check.id, check.userId, check.url.get, check.content.get)
                  )

                case CheckType.Ping =>
                  checkPing ? DoPingCheck(
                    PingCheckModel(check.id, check.userId, check.ip.get, check.port.get)
                  )
              }

              result
                .recover {
                  case _ =>
                    FailedCheckResult.apply(check.id, "We've encountered error on our side :(")
                }
                .flatMap {
                  case SuccessCheckResult(id) =>
                    jobRepo.updateCheckEventAsCompleted(event.id, isSuccessful = true, "Everything is cool :)")
                  case FailedCheckResult(id, message) =>
                    jobRepo
                      .updateCheckEventAsCompleted(event.id, isSuccessful = false, message)
                      .flatMap(
                        _ =>
                          serviceLocator.notificationClient.sendFailureReport(
                            check.userId,
                            check.id,
                            s"""
                             | Your check: ${check.friendlyName} failed.
                             | Message: $message
                             | Date: ${event.createdAt}
                           """.stripMargin
                        )
                      )
                }
            })

          Future.sequence(sequence)
        }
      )
      checkRound
        .flatMap(_ => jobRepo.notStartedExist())
        .map(notStartedExists => if (notStartedExists) self ! StartCheckCycle else ())
      ()
    case _ => println("This actor has received an invalid message")
  }

  def lookForNewCheckEvents(): Future[Unit] =
    for {
      outdatedChecks <- checkRepo.getOutdatedChecks(OffsetDateTime.now())
      _              <- jobRepo.createCheckEvents(outdatedChecks.map(_.id))
      _              <- checkRepo.updateCheckLastCheckDate(outdatedChecks.map(_.id))
    } yield ()
}

sealed trait CheckResult { val checkId: UUID }
case class SuccessCheckResult(checkId: UUID)                 extends CheckResult
case class FailedCheckResult(checkId: UUID, message: String) extends CheckResult
