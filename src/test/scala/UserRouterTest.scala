import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.AuthorizationFailedRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import backsapc.healthchecker.domain.Account
import backsapc.healthchecker.user.Contracts.TokenServiceOperationResults.{GenerateSuccess, NoSuchUserError, WrongPasswordError}
import backsapc.healthchecker.user.Contracts.UserServiceOperationResults.{EmailConflict, IdConflict, LoginConflict, RegisterSuccess}
import backsapc.healthchecker.user.Contracts.{AccountViewModel, TokenService, UserService}
import backsapc.healthchecker.user.{JsonSupport, LoginRequest, RegisterRequest, UserRouter}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

import scala.concurrent.Future

class UserRouterTest extends FlatSpec with Matchers with ScalatestRouteTest with MockFactory with JsonSupport {
  val mockTokenService: TokenService = stub[TokenService]
  val mockUserService: UserService = stub[UserService]

  val userRouter = new UserRouter(mockTokenService, mockUserService)

  val testAcc = Account(UUID.fromString("4485936a-6271-4964-a406-ed1ca9cf194f"), login = "usver", password = "password", email = "mail@com.com")

  "Route /user " should " create user" in {
    val regReq = RegisterRequest(testAcc.id, testAcc.login, testAcc.password, testAcc.email)
    val testAccVM = AccountViewModel(testAcc.id, testAcc.login, testAcc.email)
    (mockUserService.register _).when(*).returns(Future(RegisterSuccess(testAccVM)))

    Post("/user", regReq) ~> userRouter.routes ~> check {
      responseAs[String] shouldEqual s"${testAccVM.toJson}"
    }
  }

  "Route /user " should " fail cause user with login exists" in {
    val regReq = RegisterRequest(testAcc.id, testAcc.login, testAcc.password, testAcc.email)
    val loginConflict = LoginConflict(testAcc.login)
    (mockUserService.register _).when(*).returns(Future(loginConflict))

    Post("/user", regReq) ~> userRouter.routes ~> check {
      responseAs[String] shouldEqual s"${loginConflict.toJson}"
    }
  }

  "Route /user " should " fail cause user with email exists" in {
    val regReq = RegisterRequest(testAcc.id, testAcc.login, testAcc.password, testAcc.email)
    val emailConflict = EmailConflict(testAcc.email)
    (mockUserService.register _).when(*).returns(Future(emailConflict))

    Post("/user", regReq) ~> userRouter.routes ~> check {
      responseAs[String] shouldEqual emailConflict.toJson.toString
    }
  }

  "Route /user " should " fail cause user with id exists" in {
    val regReq = RegisterRequest(testAcc.id, testAcc.login, testAcc.password, testAcc.email)
    val idConflict = IdConflict(testAcc.id)
    (mockUserService.register _).when(*).returns(Future(idConflict))

    Post("/user", regReq) ~> userRouter.routes ~> check {
      responseAs[String] shouldEqual idConflict.toJson.toString
    }
  }

  "Route /token" should " return token header" in {
    val logReq = LoginRequest(testAcc.login, testAcc.password)
    val generateSuccess = GenerateSuccess("mocked token")
    (mockTokenService.generate _).when(*).returns(Future(generateSuccess))

    Post("/token", logReq) ~> userRouter.routes ~> check {
      response.status shouldEqual StatusCodes.Created
      responseAs[String] shouldEqual generateSuccess.toJson.toString
    }
  }

  "Route /token" should " fail cause wrong password" in {
    val logReq = LoginRequest(testAcc.login, testAcc.password)
    val wrongPasswordError = WrongPasswordError(testAcc.password)
    (mockTokenService.generate _).when(*).returns(Future(wrongPasswordError))

    Post("/token", logReq) ~> userRouter.routes ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

  "Route /token" should " fail cause invalid user" in {
    val logReq = LoginRequest(testAcc.login, testAcc.password)
    val noSuchUserError = NoSuchUserError(testAcc.login)
    (mockTokenService.generate _).when(*).returns(Future(noSuchUserError))

    Post("/token", logReq) ~> userRouter.routes ~> check {
      response.status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual noSuchUserError.toJson.toString
    }
  }
}
