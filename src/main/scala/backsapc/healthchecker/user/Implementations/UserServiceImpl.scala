package backsapc.healthchecker.user.Implementations

import java.util.UUID

import backsapc.healthchecker.common.Config
import backsapc.healthchecker.dao.AccountRepository
import backsapc.healthchecker.domain.Account
import backsapc.healthchecker.user.Contracts.UserService
import backsapc.healthchecker.user.RegisterRequest

import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl(accountRepository: AccountRepository)(implicit executionContext: ExecutionContext) extends Config with UserService {
  def register(account: RegisterRequest): Future[Either[String, Account]] = {
    val existsWithLogin = accountRepository.existsWithLogin(account.login)
    val existsWithEmail = accountRepository.existsWithEmail(account.email)
    val existsWithId = accountRepository.existsWithId(account.id)

    val conflicts: Future[Either[String, Unit]] = for {
      resLogin <- existsWithLogin
      resEmail <- existsWithEmail
      resId <- existsWithId
      conflicted = checkForConflicts(resLogin, resEmail, resId)
    } yield conflicted

    conflicts flatMap {
      case Right(_) => accountRepository.add(mapRegisterToAccount(account)) map (Right(_))
      case Left(reason) => Future successful Left(reason)
    }
  }

  def update(id: UUID, oldPassword: String, newPassword: String): Future[Either[String, Account]] =
    accountRepository.getById(id) flatMap {
      case Some(user) if user.password == oldPassword =>
        accountRepository.updatePassword(id, newPassword) map (Right(_))
      case Some(user) if user.password != oldPassword => Future successful Left("Invalid password")
      case None => Future successful Left("Invalid user id")
    }

  def findById(id: UUID): Future[Option[Account]] = accountRepository.getById(id)

  def findByLogin(login: String): Future[Option[Account]] = accountRepository.getByLogin(login)

  private def mapRegisterToAccount(registerRequest: RegisterRequest): Account =
    Account(registerRequest.id, registerRequest.login, registerRequest.password, registerRequest.email)

  private def checkForConflicts(loginConflict: Boolean, emailConflict: Boolean, idConflict: Boolean): Either[String, Unit] =
    if (loginConflict)
      Left("User with same login exists")
    else if (emailConflict)
      Left("User with same email exists")
    else if (idConflict)
      Left("User with same id exists")
    else
      Right(())
}
