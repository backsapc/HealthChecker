package backsapc.healthchecker.notification.contracts
import scala.concurrent.Future

trait MailService {
  def sendEmail(email: String, subject: String, message: String): Future[Unit]
}
