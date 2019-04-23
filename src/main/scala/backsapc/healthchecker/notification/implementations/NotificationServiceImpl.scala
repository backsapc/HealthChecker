package backsapc.healthchecker.notification.implementations
import java.util.UUID

import backsapc.healthchecker.common.Config
import backsapc.healthchecker.notification.contracts.{
  InvalidConfirmationCodeException,
  MailService,
  NoSuchUserException,
  NotificationService
}
import backsapc.healthchecker.notification.dao.{ Channel, NotificationRepository }

import scala.concurrent.{ ExecutionContext, Future }

class NotificationServiceImpl(repository: NotificationRepository,
                              tokenGenerator: TokenGenerator,
                              mailService: MailService)(
    implicit executionContext: ExecutionContext
) extends Config
    with NotificationService {

  override def sendFailNotification(userId: UUID, message: String): Future[Unit] =
    repository.getNotificationChannel(userId).flatMap {
      case Some(channel) => mailService.sendEmail(channel.email, "Bad news about your health check", message)
      case None          => Future failed NoSuchUserException(userId)
    }

  def createChannel(userId: UUID, email: String): Future[Channel] =
    repository.addNotificationChannel(userId, email, tokenGenerator.generateMD5Token(s"$userId$email"))

  override def sendConfirmationEmail(userId: UUID): Future[Unit] =
    repository.getNotificationChannel(userId).flatMap {
      case Some(channel) =>
        mailService.sendEmail(
          channel.email,
          "Welcome to top#1 health check service",
          s"""Click <a href="http://$domain/confirm/$userId/${channel.id}/${channel.confirmationCode}">here</a> to confirm registration."""
        )
      case None => Future failed NoSuchUserException(userId)
    }

  override def confirmUserEmail(
      userId: UUID,
      channelId: Long,
      confirmationCode: String
  ): Future[Channel] = repository.getNotificationChannel(userId).flatMap {
    case Some(channel) =>
      if (channel.confirmationCode == confirmationCode)
        repository
          .confirmNotificationChannel(userId, channelId)
      else
        Future failed InvalidConfirmationCodeException(confirmationCode)
    case None => Future failed NoSuchUserException(userId)
  }
}
