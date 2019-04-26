package backsapc.healthchecker.user.Contracts
import java.util.UUID

import scala.concurrent.Future

trait NotificationClient {
  def createChannelAndSendConfirmation(userId: UUID, email: String): Future[Unit]
}
