import java.util.UUID

import backsapc.healthchecker.dao.AccountRepository
import backsapc.healthchecker.domain.Account
import backsapc.healthchecker.user.Contracts.AccountViewModel
import backsapc.healthchecker.user.Contracts.UserServiceOperationResults._
import backsapc.healthchecker.user.Implementations.UserServiceImpl
import backsapc.healthchecker.user.RegisterRequest
import backsapc.healthchecker.user.bcrypt.AsyncBcrypt
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.Future

class UserServiceImplTest extends AsyncFlatSpec with AsyncMockFactory with Matchers {
  val mockAccountRepository: AccountRepository = stub[AccountRepository]
  val mockAsyncBcrypt: AsyncBcrypt = stub[AsyncBcrypt]
  val userService: UserServiceImpl = new UserServiceImpl(mockAccountRepository, mockAsyncBcrypt)

  val testAcc = Account(UUID.fromString("4485936a-6271-4964-a406-ed1ca9cf194f"), login = "usver", password = "password", email = "mail@com.com")

  behavior of "kek"

  def mapToAccountVM(account: Account): AccountViewModel =
    AccountViewModel(account.id, account.login, account.email)

  "User service " should " register user" in {
    (mockAccountRepository.add _).when(*).returns(Future successful testAcc)
    (mockAccountRepository.existsWithLogin _).when(*).returns(Future successful false)
    (mockAccountRepository.existsWithEmail _).when(*).returns(Future successful false)
    (mockAccountRepository.existsWithId _).when(*).returns(Future successful false)
    userService.register(RegisterRequest(testAcc.id, testAcc.login, testAcc.password, testAcc.email))
      .map(_ shouldBe RegisterSuccess(mapToAccountVM(testAcc)))
  }

  "User service " should " fail because same id" in {
    (mockAccountRepository.add _).when(*).returns(Future successful testAcc)
    (mockAccountRepository.existsWithLogin _).when(*).returns(Future successful false)
    (mockAccountRepository.existsWithEmail _).when(*).returns(Future successful false)
    (mockAccountRepository.existsWithId _).when(*).returns(Future successful true)
    userService.register(RegisterRequest(testAcc.id, testAcc.login, testAcc.password, testAcc.email))
      .map(_ shouldBe IdConflict(testAcc.id))
  }

  "User service " should " fail because same login" in {
    (mockAccountRepository.add _).when(*).returns(Future successful testAcc)
    (mockAccountRepository.existsWithLogin _).when(*).returns(Future successful true)
    (mockAccountRepository.existsWithEmail _).when(*).returns(Future successful false)
    (mockAccountRepository.existsWithId _).when(*).returns(Future successful false)
    userService.register(RegisterRequest(testAcc.id, testAcc.login, testAcc.password, testAcc.email))
      .map(_ shouldBe LoginConflict(testAcc.login))
  }

  "User service " should " fail because same email" in {
    (mockAccountRepository.add _).when(*).returns(Future successful testAcc)
    (mockAccountRepository.existsWithLogin _).when(*).returns(Future successful false)
    (mockAccountRepository.existsWithEmail _).when(*).returns(Future successful true)
    (mockAccountRepository.existsWithId _).when(*).returns(Future successful false)
    userService.register(RegisterRequest(testAcc.id, testAcc.login, testAcc.password, testAcc.email))
      .map(_ shouldBe EmailConflict(testAcc.email))
  }

  "User service " should s" return user with id: ${testAcc.id}" in {
    (mockAccountRepository.getById _).when(*).returns(Future successful Some(testAcc))
    userService.findById(testAcc.id) map (_ shouldBe Some(mapToAccountVM(testAcc)))
  }

  "User service " should s" not found user with id: ${testAcc.id}" in {
    (mockAccountRepository.getById _).when(*).returns(Future successful None)
    userService.findById(testAcc.id) map (_ shouldBe None)
  }

  "User service " should s" return user with id: ${testAcc.login}" in {
    (mockAccountRepository.getByLogin _).when(*).returns(Future successful Some(testAcc))
    userService.findByLogin(testAcc.login) map (_ shouldBe Some(mapToAccountVM(testAcc)))
  }

  "User service " should s" not found user with id: ${testAcc.login}" in {
    (mockAccountRepository.getByLogin _).when(*).returns(Future successful None)
    userService.findByLogin(testAcc.login) map (_ shouldBe None)
  }

  "User service " should " update user password" in {
    val testPassword = "some unique pass"
    (mockAsyncBcrypt.hash _).when(*, *).returns(Future successful testPassword)
    (mockAsyncBcrypt.verify _).when(*, *).returns(Future successful true)
    (mockAccountRepository.getById _).when(*).returns(Future successful Some(testAcc))
    (mockAccountRepository.updatePassword _).when(*, *).returns(Future successful testAcc.copy(password = testPassword))
    userService.update(testAcc.id, testAcc.password, testPassword)
      .map(_ shouldBe UpdateSuccess())
  }

  "User service " should " fail because invalid user id" in {
    val testPassword = "some unique pass"
    (mockAccountRepository.getById _).when(*).returns(Future successful None)
    userService.update(testAcc.id, testAcc.password, testPassword)
      .map(_ shouldBe NoSuchUserError(testAcc.id))
  }

  "User service " should " fail because invalid old password" in {
    val testPassword = "some unique pass"
    (mockAsyncBcrypt.hash _).when(*, *).returns(Future successful testPassword)
    (mockAsyncBcrypt.verify _).when(*, *).returns(Future successful false)
    (mockAccountRepository.getById _).when(*).returns(Future successful Some(testAcc))
    userService.update(testAcc.id, testPassword, testPassword)
      .map(_ shouldBe InvalidPassword(testPassword))
  }

  "User service " should " fail with NOSuchElementException because of repository inconsistency" in {
    val testPassword = "some unique pass"
    (mockAsyncBcrypt.hash _).when(*, *).returns(Future successful testPassword)
    (mockAsyncBcrypt.verify _).when(*, *).returns(Future successful true)
    (mockAccountRepository.getById _).when(*).returns(Future successful Some(testAcc))
    (mockAccountRepository.updatePassword _).when(*, *).returns(Future.failed(new NoSuchElementException))
    userService.update(testAcc.id, testAcc.password, testPassword).recover { case _: NoSuchElementException => true }.map {
      _ shouldBe true
    }
  }
}
