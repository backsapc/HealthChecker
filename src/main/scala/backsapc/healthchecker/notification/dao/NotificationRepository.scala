package backsapc.healthchecker.notification.dao
import java.util.UUID

import scala.concurrent.Future

trait NotificationRepository {
  def addNotificationChannel(userId: UUID, email: String, confirmationCode: String): Future[Channel]
  def deleteNotificationChannel(userId: UUID): Future[Unit]
  def confirmNotificationChannel(userId: UUID, channelId: Long): Future[Channel]
  def getNotificationChannel(userID: UUID): Future[Option[Channel]]
}

case class Channel(id: Long,
                   userId: UUID,
                   email: String,
                   isConfirmed: Boolean,
                   isDeleted: Boolean,
                   confirmationCode: String)
