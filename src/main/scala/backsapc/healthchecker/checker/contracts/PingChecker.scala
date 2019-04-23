package backsapc.healthchecker.checker.contracts

import backsapc.healthchecker.checker.domain.PingCheckModel
import backsapc.healthchecker.checker.jobs.CheckResult

import scala.concurrent.Future

trait PingChecker {
  def doCheck(check: PingCheckModel): Future[CheckResult]
}
