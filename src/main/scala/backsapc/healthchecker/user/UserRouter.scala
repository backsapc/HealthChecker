package backsapc.healthchecker.user

import java.util.UUID

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import backsapc.healthchecker.common.JwtService
import backsapc.healthchecker.user.Contracts.{TokenService, UserService}

import scala.concurrent.ExecutionContext

case class RegisterRequest(id: UUID, login: String, password: String, email: String)

case class LoginRequest(login: String, password: String)

class UserRouter(tokenService: TokenService, userService: UserService)
                (implicit executionContext: ExecutionContext) extends JwtService with JsonSupport {

  val routes: Route = path("user") {
    post {
      entity(as[RegisterRequest]) { register =>
        complete {
          userService.register(register).map[ToResponseMarshallable] {
            case Right(account) => StatusCodes.Created -> account
            case Left(error) => StatusCodes.Conflict -> error
          }
        }
      }
    } ~ (get & authenticated) { claims =>
      complete {
        getLogin(claims) match {
          case Some(login) => userService.findByLogin(login).map[ToResponseMarshallable] {
            case Some(account) => StatusCodes.Created -> account
            case None => StatusCodes.NotFound
          }
          case None => StatusCodes.NotFound
        }
      }
    }
  } ~ path("token") {
    post {
      entity(as[LoginRequest]) { login =>
        complete {
          tokenService.generate(login).map[ToResponseMarshallable] {
            case Right(token) => StatusCodes.Created -> token
            case Left(error) => StatusCodes.Unauthorized -> error
          }
        }
      }
    }
  }
}
