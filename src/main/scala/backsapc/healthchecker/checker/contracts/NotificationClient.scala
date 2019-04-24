package backsapc.healthchecker.checker.contracts
import java.util.UUID

import scala.concurrent.Future

trait NotificationClient {
  def sendFailureReport(userId: UUID, checkId: UUID, message: String): Future[Unit]
}
