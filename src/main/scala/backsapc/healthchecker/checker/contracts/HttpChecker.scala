package backsapc.healthchecker.checker.contracts
import backsapc.healthchecker.checker.domain.HttpCheckModel
import backsapc.healthchecker.checker.jobs.CheckResult

import scala.concurrent.Future

trait HttpChecker {
  def doCheck(check: HttpCheckModel): Future[CheckResult]
}
