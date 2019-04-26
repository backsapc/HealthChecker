package backsapc.healthchecker.notification
import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import backsapc.healthchecker.common.JwtService
import backsapc.healthchecker.notification.contracts.{
  InvalidConfirmationCodeException,
  NoSuchUserException,
  NotificationService
}

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

case class ConfirmationErrorResult(message: String)
case class ConfirmationSuccessResult(message: String)

class NotificationRouter(notificationService: NotificationService)(implicit executionContext: ExecutionContext)
    extends JwtService
    with NotificationJsonSupport {
  val routes: Route = path("confirm" / JavaUUID / LongNumber / Segment) {
    (userId: UUID, channelId: Long, key: String) =>
      get {
        onComplete(notificationService.confirmUserEmail(userId, channelId, key)) {
          case Success(channel) =>
            complete(StatusCodes.OK -> ConfirmationSuccessResult(s"Congrats! ${channel.email} is assigned to you!"))
          case Failure(exception) =>
            exception match {
              case NoSuchUserException(id) =>
                complete(StatusCodes.BadRequest -> ConfirmationErrorResult(s"Invalid user id: $id"))
              case InvalidConfirmationCodeException(code) =>
                complete(StatusCodes.BadRequest -> ConfirmationErrorResult(s"Invalid confirmation code: $code"))
            }
        }
      }
  }
}
