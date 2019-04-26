package backsapc.healthchecker.checker

import java.util.UUID

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import backsapc.healthchecker.checker.contracts.CheckerService
import backsapc.healthchecker.common.JwtService
import spray.json.DefaultJsonProtocol._

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

case class CreateHttpCheck(id: UUID, interval: Int, friendlyName: String, url: String)
case class UpdateHttpCheck(id: UUID, interval: Int, friendlyName: String, url: String)
case class CreateContentCheck(
    id: UUID,
    friendlyName: String,
    interval: Int,
    url: String,
    content: String
)
case class UpdateContentCheck(
    id: UUID,
    friendlyName: String,
    interval: Int,
    url: String,
    content: String
)
case class CreatePingCheck(id: UUID, interval: Int, friendlyName: String, ip: String, port: Int)
case class UpdatePingCheck(id: UUID, interval: Int, friendlyName: String, ip: String, port: Int)

class CheckerRouter(checkerService: CheckerService)(
    implicit executionContext: ExecutionContext
) extends JwtService
    with CheckerJsonSupport {

  val routes: Route = (path("check" / "http") & authenticated) { claims =>
    post {
      entity(as[CreateHttpCheck]) { createHttpCheck =>
        complete {
          checkerService
            .createHttpCheck(createHttpCheck, getId(claims))
            .map[ToResponseMarshallable](
              result => StatusCodes.Created -> result
            )
        }
      }
    } ~ put {
      entity(as[UpdateHttpCheck]) { updateHttpCheck =>
        complete {
          checkerService
            .updateHttpCheck(updateHttpCheck, getId(claims))
            .map[ToResponseMarshallable](
              result => StatusCodes.Created -> result
            )
        }
      }
    }
  } ~ (path("check" / "content") & authenticated) { claims =>
    post {
      entity(as[CreateContentCheck]) { createContentCheck =>
        complete {
          checkerService
            .createContentCheck(createContentCheck, getId(claims))
            .map[ToResponseMarshallable](
              result => StatusCodes.Created -> result
            )
        }
      }
    } ~ put {
      entity(as[UpdateContentCheck]) { updateContentCheck =>
        complete {
          checkerService
            .updateContentCheck(updateContentCheck, getId(claims))
            .map[ToResponseMarshallable](
              result => StatusCodes.Created -> result
            )
        }
      }
    }
  } ~ (path("check" / "ping") & authenticated) { claims =>
    post {
      entity(as[CreatePingCheck]) { createPingCheck =>
        complete {
          checkerService
            .createPingCheck(createPingCheck, getId(claims))
            .map[ToResponseMarshallable](
              result => StatusCodes.Created -> result
            )
        }
      }
    } ~ put {
      entity(as[UpdatePingCheck]) { updatePingCheck =>
        complete {
          checkerService
            .updatePingCheck(updatePingCheck, getId(claims))
            .map[ToResponseMarshallable](
              result => StatusCodes.Created -> result
            )
        }
      }
    }
  } ~ (path("check") & authenticated) { claims =>
    (get & pathEndOrSingleSlash) {
      onComplete(checkerService.getAllForUserId(getId(claims))) {
        case Success(value) => complete(value)
        case Failure(_)     => complete(StatusCodes.InternalServerError)
      }
    }
  } ~ (path("check" / JavaUUID) & authenticated) { (id, claims) =>
    get {
      complete {
        checkerService
          .getById(id, getId(claims))
          .map[ToResponseMarshallable] {
            case Some(check) => StatusCodes.OK -> check
            case None        => StatusCodes.NotFound
          }
      }
    } ~ delete {
      onComplete(checkerService.delete(id, getId(claims))) {
        case Success(_) => complete(StatusCodes.NoContent)
        case Failure(_) => complete(StatusCodes.NotFound)
      }
    }
  }
}
