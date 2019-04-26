package backsapc.healthchecker.checker.contracts

import backsapc.healthchecker.checker.domain.HttpContentCheckModel
import backsapc.healthchecker.checker.jobs.CheckResult

import scala.concurrent.Future

trait HttpContentChecker {
  def doCheck(check: HttpContentCheckModel): Future[CheckResult]
}
