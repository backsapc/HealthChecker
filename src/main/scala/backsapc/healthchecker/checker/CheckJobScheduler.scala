package backsapc.healthchecker.checker
import akka.actor.{ ActorSystem, Cancellable }
import backsapc.healthchecker.checker.contracts.{ HttpChecker, HttpContentChecker, PingChecker }
import backsapc.healthchecker.checker.dao.{ CheckerJobRepository, CheckerRepository }
import backsapc.healthchecker.checker.jobs.{ CheckJob, StartCheckCycle }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class CheckJobScheduler(implicit system: ActorSystem, executionContext: ExecutionContext) {
  def run(checkerRepository: CheckerRepository,
          jobRepository: CheckerJobRepository,
          httpChecker: HttpChecker,
          contentChecker: HttpContentChecker,
          pingChecker: PingChecker): Cancellable = {
    val simpleJob =
      system.actorOf(CheckJob.props(checkerRepository, jobRepository, httpChecker, contentChecker, pingChecker),
                     "check-job")

    system.scheduler.schedule(0.milliseconds, 10.seconds, simpleJob, StartCheckCycle)
  }
}
