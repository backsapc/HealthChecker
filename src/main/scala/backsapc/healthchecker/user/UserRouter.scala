package backsapc.healthchecker.user

import java.util.UUID

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Route}
import backsapc.healthchecker.common.JwtService
import backsapc.healthchecker.user.Contracts.TokenServiceOperationResults.{
  GenerateSuccess,
  NoSuchUserError,
  WrongPasswordError
}
import backsapc.healthchecker.user.Contracts.UserServiceOperationResults._
import backsapc.healthchecker.user.Contracts.{TokenService, UserService}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

case class RegisterRequest(
  id: UUID,
  login: String,
  password: String,
  email: String
)

case class LoginRequest(login: String, password: String)

class UserRouter(tokenService: TokenService, userService: UserService)(
  implicit executionContext: ExecutionContext
) extends JwtService
    with UserJsonSupport {

  val routes: Route = path("user") {
    post {
      entity(as[RegisterRequest]) { register =>
        complete {
          userService.register(register).map[ToResponseMarshallable] {
            case RegisterSuccess(account) => StatusCodes.Created             -> account
            case error: LoginConflict     => StatusCodes.Conflict            -> error
            case error: EmailConflict     => StatusCodes.Conflict            -> error
            case error: IdConflict        => StatusCodes.Conflict            -> error
            case x: RegisterResult        => StatusCodes.InternalServerError -> x
          }
        }
      }
    } ~ (get & authenticated) { claims =>
      complete {
        userService.findById(getId(claims)).map[ToResponseMarshallable] {
          case Some(account) => StatusCodes.Created -> account
          case None          => StatusCodes.NotFound
        }
      }
    }
  } ~ path("token") {
    post {
      entity(as[LoginRequest]) { login =>
        onComplete(tokenService.generate(login)) {
          case Success(value) =>
            value match {
              case success: GenerateSuccess =>
                complete(StatusCodes.Created -> success)
              case noSuchUser: NoSuchUserError =>
                complete(StatusCodes.NotFound -> noSuchUser)
              case _: WrongPasswordError => reject(AuthorizationFailedRejection)
              case x                     => complete(StatusCodes.InternalServerError -> x)
            }
          case Failure(_) => complete(StatusCodes.InternalServerError)
        }
      }
    }
  }
}
