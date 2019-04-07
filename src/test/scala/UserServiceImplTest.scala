import java.util.UUID

import backsapc.healthchecker.dao.AccountRepository
import backsapc.healthchecker.domain.Account
import backsapc.healthchecker.user.Implementations.UserServiceImpl
import backsapc.healthchecker.user.RegisterRequest
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.Future

class UserServiceImplTest extends AsyncFlatSpec with AsyncMockFactory with Matchers {
  val mockAccountRepository: AccountRepository = stub[AccountRepository]
  val userService: UserServiceImpl = new UserServiceImpl(mockAccountRepository)

  val testAcc = Account(UUID.fromString("4485936a-6271-4964-a406-ed1ca9cf194f"), login = "usver", password = "password", email = "mail@com.com")

  behavior of "kek"

  "User service " should " register user" in {
    (mockAccountRepository.add _).when(*).returns(Future successful testAcc)
    (mockAccountRepository.existsWithLogin _).when(*).returns(Future successful false)
    (mockAccountRepository.existsWithEmail _).when(*).returns(Future successful false)
    (mockAccountRepository.existsWithId _).when(*).returns(Future successful false)
    userService.register(RegisterRequest(testAcc.id, testAcc.login, testAcc.password, testAcc.email)) map (_ shouldBe Right(testAcc))
  }

  "User service " should " fail because same id" in {
    (mockAccountRepository.add _).when(*).returns(Future successful testAcc)
    (mockAccountRepository.existsWithLogin _).when(*).returns(Future successful false)
    (mockAccountRepository.existsWithEmail _).when(*).returns(Future successful false)
    (mockAccountRepository.existsWithId _).when(*).returns(Future successful true)
    userService.register(RegisterRequest(testAcc.id, testAcc.login, testAcc.password, testAcc.email)) map (_ shouldBe Left("User with same id exists"))
  }

  "User service " should " fail because same login" in {
    (mockAccountRepository.add _).when(*).returns(Future successful testAcc)
    (mockAccountRepository.existsWithLogin _).when(*).returns(Future successful true)
    (mockAccountRepository.existsWithEmail _).when(*).returns(Future successful false)
    (mockAccountRepository.existsWithId _).when(*).returns(Future successful false)
    userService.register(RegisterRequest(testAcc.id, testAcc.login, testAcc.password, testAcc.email)) map (_ shouldBe Left("User with same login exists"))
  }

  "User service " should " fail because same email" in {
    (mockAccountRepository.add _).when(*).returns(Future successful testAcc)
    (mockAccountRepository.existsWithLogin _).when(*).returns(Future successful false)
    (mockAccountRepository.existsWithEmail _).when(*).returns(Future successful true)
    (mockAccountRepository.existsWithId _).when(*).returns(Future successful false)
    userService.register(RegisterRequest(testAcc.id, testAcc.login, testAcc.password, testAcc.email)) map (_ shouldBe Left("User with same email exists"))
  }

  "User service " should s" return user with id: ${testAcc.id}" in {
    (mockAccountRepository.getById _).when(*).returns(Future successful Some(testAcc))
    userService.findById(testAcc.id) map (_ shouldBe Some(testAcc))
  }

  "User service " should s" not found user with id: ${testAcc.id}" in {
    (mockAccountRepository.getById _).when(*).returns(Future successful None)
    userService.findById(testAcc.id) map (_ shouldBe None)
  }

  "User service " should s" return user with id: ${testAcc.login}" in {
    (mockAccountRepository.getByLogin _).when(*).returns(Future successful Some(testAcc))
    userService.findByLogin(testAcc.login) map (_ shouldBe Some(testAcc))
  }

  "User service " should s" not found user with id: ${testAcc.login}" in {
    (mockAccountRepository.getByLogin _).when(*).returns(Future successful None)
    userService.findByLogin(testAcc.login) map (_ shouldBe None)
  }

  "User service " should " update user password" in {
    val testPassword = "some unique pass"
    (mockAccountRepository.getById _).when(*).returns(Future successful Some(testAcc))
    (mockAccountRepository.updatePassword _).when(*, *).returns(Future successful testAcc.copy(password = testPassword))
    userService.update(testAcc.id, testAcc.password, testPassword)
      .map(_ shouldBe Right(testAcc.copy(password = testPassword)))
  }

  "User service " should " fail because invalid user id" in {
    val testPassword = "some unique pass"
    (mockAccountRepository.getById _).when(*).returns(Future successful None)
    userService.update(testAcc.id, testAcc.password, testPassword)
      .map(_ shouldBe Left("Invalid user id"))
  }

  "User service " should " fail because invalid old password" in {
    val testPassword = "some unique pass"
    (mockAccountRepository.getById _).when(*).returns(Future successful None)
    userService.update(testAcc.id, testPassword, testPassword)
      .map(_ shouldBe Left("Invalid user id"))
  }

  "User service " should " fail with NOSuchElementException because of repository inconsistency" in {
    val testPassword = "some unique pass"
    (mockAccountRepository.getById _).when(*).returns(Future successful Some(testAcc))
    (mockAccountRepository.updatePassword _).when(*, *).returns(Future.failed(new NoSuchElementException))
    userService.update(testAcc.id, testAcc.password, testPassword).recover { case _: NoSuchElementException => true }.map {
      _ shouldBe true
    }
  }
}
