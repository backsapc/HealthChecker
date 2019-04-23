package backsapc.healthchecker.user.Implementations
import java.util.UUID

import backsapc.healthchecker.notification.contracts.NotificationService
import backsapc.healthchecker.user.Contracts.NotificationClient

import scala.concurrent.{ ExecutionContext, Future }

class NotificationClientImpl(notificationService: NotificationService)(implicit executionContext: ExecutionContext)
    extends NotificationClient {
  override def createChannelAndSendConfirmation(
      userId: UUID,
      email: String
  ): Future[Unit] = notificationService.createChannel(userId, email).flatMap { _ =>
    notificationService.sendConfirmationEmail(userId)
  }
}
