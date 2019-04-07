import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import backsapc.healthchecker.domain.Account
import backsapc.healthchecker.user.Contracts.{TokenService, UserService}
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
    (mockUserService.register _).when(*).returns(Future(Right(testAcc)))

    Post("/user", regReq) ~> userRouter.routes ~> check {
      responseAs[String] shouldEqual s"${testAcc.toJson}"
    }
  }

  "Route /user " should " fail cause user with login exists" in {
    val regReq = RegisterRequest(testAcc.id, testAcc.login, testAcc.password, testAcc.email)
    (mockUserService.register _).when(*).returns(Future(Left("User with same login exists")))

    Post("/user", regReq) ~> userRouter.routes ~> check {
      responseAs[String] shouldEqual """User with same login exists"""
    }
  }

  "Route /user " should " fail cause user with email exists" in {
    val regReq = RegisterRequest(testAcc.id, testAcc.login, testAcc.password, testAcc.email)
    (mockUserService.register _).when(*).returns(Future(Left("User with same email exists")))

    Post("/user", regReq) ~> userRouter.routes ~> check {
      responseAs[String] shouldEqual """User with same email exists"""
    }
  }

  "Route /user " should " fail cause user with id exists" in {
    val regReq = RegisterRequest(testAcc.id, testAcc.login, testAcc.password, testAcc.email)
    (mockUserService.register _).when(*).returns(Future(Left("User with same id exists")))

    Post("/user", regReq) ~> userRouter.routes ~> check {
      responseAs[String] shouldEqual """User with same id exists"""
    }
  }

  "Route /token" should " return token header" in {
    val logReq = LoginRequest(testAcc.login, testAcc.password)
    (mockTokenService.generate _).when(*).returns(Future(Right("mocked token")))

    Post("/token", logReq) ~> userRouter.routes ~> check {
      response.status shouldEqual StatusCodes.Created
      responseAs[String] shouldEqual """mocked token"""
    }
  }

  "Route /token" should " fail cause wrong password" in {
    val logReq = LoginRequest(testAcc.login, testAcc.password)
    (mockTokenService.generate _).when(*).returns(Future(Left("Wrong password")))

    Post("/token", logReq) ~> userRouter.routes ~> check {
      response.status shouldEqual StatusCodes.Unauthorized
      responseAs[String] shouldEqual """Wrong password"""
    }
  }

  "Route /token" should " fail cause invalid user" in {
    val logReq = LoginRequest(testAcc.login, testAcc.password)
    (mockTokenService.generate _).when(*).returns(Future(Left("No such user")))

    Post("/token", logReq) ~> userRouter.routes ~> check {
      response.status shouldEqual StatusCodes.Unauthorized
      responseAs[String] shouldEqual """No such user"""
    }
  }
}
