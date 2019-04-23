package backsapc.healthchecker.checker.jobs
import java.time.OffsetDateTime
import java.util.UUID

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout
import backsapc.healthchecker.checker.contracts.{ HttpChecker, HttpContentChecker, PingChecker }
import backsapc.healthchecker.checker.dao.{ CheckerJobRepository, CheckerRepository }
import backsapc.healthchecker.checker.domain.{ CheckType, HttpCheckModel, HttpContentCheckModel, PingCheckModel }
import backsapc.healthchecker.checker.jobs.CheckContentActor.DoHttpContentCheck
import backsapc.healthchecker.checker.jobs.CheckHttpActor.DoHttpCheck
import backsapc.healthchecker.checker.jobs.CheckPingActor.DoPingCheck

import scala.concurrent.Future
import scala.concurrent.duration._

object CheckJob {
  def props(repository: CheckerRepository,
            jobRepository: CheckerJobRepository,
            httpChecker: HttpChecker,
            contentChecker: HttpContentChecker,
            pingChecker: PingChecker): Props =
    Props(new CheckJob(repository, jobRepository, httpChecker, contentChecker, pingChecker))
}

case object StartCheckCycle
case class CheckFailed(checkId: UUID)

class CheckJob(checkRepo: CheckerRepository,
               jobRepo: CheckerJobRepository,
               httpChecker: HttpChecker,
               contentChecker: HttpContentChecker,
               pingChecker: PingChecker)
    extends Actor
    with ActorLogging {

  import context.dispatcher
  implicit val timeout: Timeout = Timeout(5.seconds)

  val checkHttp: ActorRef    = context.actorOf(CheckHttpActor.props(httpChecker), "http-check-actor")
  val checkContent: ActorRef = context.actorOf(CheckContentActor.props(contentChecker), "content-check-actor")
  val checkPing: ActorRef    = context.actorOf(CheckPingActor.props(pingChecker), "ping-check-actor")

  override def receive = {
    case StartCheckCycle =>
      println("starter checks")
      lel()
        .flatMap(_ => jobRepo.getOutdatedChecks(100))
        .map(
          checks => {
            println(s"found ${checks.length} to check")
            checks.foreach {
              check =>
                check.checkType match {
                  case CheckType.Http =>
                    (checkHttp ? DoHttpCheck(HttpCheckModel(check.id, check.userId, check.url.get))).map {
                      case SuccessCheckResult(id)         => log.info(s"Http check success $id")
                      case FailedCheckResult(id, message) => log.info(s"Http check failed $id $message")
                    }

                  case CheckType.HttpWithContent =>
                    (checkContent ? DoHttpContentCheck(
                      HttpContentCheckModel(check.id, check.userId, check.url.get, check.content.get)
                    )).map {
                      case SuccessCheckResult(id)         => log.info(s"Content check success $id")
                      case FailedCheckResult(id, message) => log.info(s"Content check failed $id $message")
                    }

                  case CheckType.Ping =>
                    (checkPing ? DoPingCheck(
                      PingCheckModel(check.id, check.userId, check.ip.get, check.port.get)
                    )).map {
                      case SuccessCheckResult(id)         => log.info(s"Ping check success $id")
                      case FailedCheckResult(id, message) => log.info(s"Ping check failed $id $message")
                    }
                }
            }
          }
        )
      ()
    case _ => println("This actor has received an invalid message")
  }

  def lel(): Future[Unit] =
    checkRepo
      .getOutdatedChecks(OffsetDateTime.now())
      .flatMap(checks => checkRepo.markChecksToInProgress(checks.map(_.id)))
      .map(_ => ())
}

sealed trait CheckResult { val checkId: UUID }
case class SuccessCheckResult(checkId: UUID)                 extends CheckResult
case class FailedCheckResult(checkId: UUID, message: String) extends CheckResult
