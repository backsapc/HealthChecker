package backsapc.healthchecker.notification.implementations
import backsapc.healthchecker.common.Config
import backsapc.healthchecker.notification.contracts.MailService
import javax.mail.internet.InternetAddress

import scala.concurrent.Future

class MailServiceImpl extends MailService with Config {
  import courier._
  import Defaults._
  val mailer: Mailer = Mailer(emailServer, emailServerPort)
    .auth(true)
    .as(emailLogin, emailPassword)
    .startTls(true)()

  override def sendEmail(email: String, subject: String, message: String): Future[Unit] =
    mailer(
      Envelope
        .from(new InternetAddress(emailLogin))
        .to(new InternetAddress(email))
        .subject(subject)
        .content(Multipart().html(message))
    )
}
