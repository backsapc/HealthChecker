package backsapc.healthchecker.notification.contracts
import java.util.UUID

import backsapc.healthchecker.notification.dao.Channel

import scala.concurrent.Future

trait NotificationService {
  def createChannel(userId: UUID, email: String): Future[Channel]
  def sendConfirmationEmail(userId: UUID): Future[Unit]
  def sendFailNotification(userId: UUID, message: String): Future[Unit]
  def confirmUserEmail(userId: UUID, channelId: Long, confirmationCode: String): Future[Channel]
}

case class NoSuchUserException(userId: UUID)              extends Exception
case class InvalidConfirmationCodeException(code: String) extends Exception
