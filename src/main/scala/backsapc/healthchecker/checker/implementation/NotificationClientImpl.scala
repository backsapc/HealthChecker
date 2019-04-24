package backsapc.healthchecker.checker.implementation
import java.util.UUID

import backsapc.healthchecker.checker.contracts.NotificationClient
import backsapc.healthchecker.notification.contracts.NotificationService

import scala.concurrent.{ ExecutionContext, Future }

class NotificationClientImpl(notificationService: NotificationService)(implicit executionContext: ExecutionContext)
    extends NotificationClient {
  override def sendFailureReport(userId: UUID, checkId: UUID, message: String): Future[Unit] =
    notificationService.sendFailNotification(userId, message)
}
