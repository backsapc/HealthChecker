package backsapc.healthchecker.user.Implementations

import java.util.UUID

import backsapc.healthchecker.common.Config
import backsapc.healthchecker.user.Contracts.UserServiceOperationResults._
import backsapc.healthchecker.user.Contracts.{ AccountViewModel, NotificationClient, UserService }
import backsapc.healthchecker.user.RegisterRequest
import backsapc.healthchecker.user.bcrypt.AsyncBcrypt
import backsapc.healthchecker.user.dao.AccountRepository
import backsapc.healthchecker.user.domain.Account

import scala.concurrent.{ ExecutionContext, Future }

class UserServiceImpl(accountRepository: AccountRepository, bСrypt: AsyncBcrypt, notificationClient: NotificationClient)(
    implicit executionContext: ExecutionContext
) extends Config
    with UserService {

  def register(account: RegisterRequest): Future[RegisterResult] = {
    val existsWithLogin = accountRepository.existsWithLogin(account.login).map {
      case true  => throw new LoginConflictException
      case false => false
    }
    val existsWithEmail = accountRepository.existsWithEmail(account.email).map {
      case true  => throw new EmailConflictException
      case false => false
    }
    val existsWithId = accountRepository.existsWithId(account.id).map {
      case true  => throw new IdConflictException
      case false => false
    }

    val registrationResult = for {
      _            <- existsWithLogin
      _            <- existsWithEmail
      _            <- existsWithId
      passwordHash <- bСrypt.hash(account.password)
      result <- accountRepository
        .add(Account(account.id, account.login, passwordHash, account.email))
        .map(accountToViewModel)
    } yield RegisterSuccess(result)

    registrationResult
      .flatMap(
        success =>
          notificationClient
            .createChannelAndSendConfirmation(account.id, account.email)
            .map(_ => success)
            .recoverWith {
              case _ => accountRepository.delete(account.id).map(_ => ConfirmationError(account.email))
          }
      )
      .recover {
        case _: LoginConflictException => LoginConflict(account.login)
        case _: EmailConflictException => EmailConflict(account.email)
        case _: IdConflictException    => IdConflict(account.id)
      }
  }

  def update(
      id: UUID,
      oldPassword: String,
      newPassword: String
  ): Future[UpdateResult] =
    accountRepository.getById(id) flatMap {
      case Some(account) => bСrypt.verify(oldPassword, account.password)
      case None          => Future.failed(throw new NoSuchUserException)
    } flatMap {
      case true =>
        bСrypt.hash(newPassword).flatMap {
          accountRepository.updatePassword(id, _).map(_ => UpdateSuccess())
        }
      case false => Future.failed(throw new VerificationFailedException)
    } recover {
      case _: NoSuchUserException         => NoSuchUserError(id)
      case _: VerificationFailedException => InvalidPassword(oldPassword)
    }

  def findById(id: UUID): Future[Option[AccountViewModel]] =
    accountRepository.getById(id).map(_.map(accountToViewModel))

  def findByLogin(login: String): Future[Option[AccountViewModel]] =
    accountRepository.getByLogin(login).map(_.map(accountToViewModel))

  private def accountToViewModel(account: Account): AccountViewModel =
    AccountViewModel(account.id, account.login, account.email)
}

class LoginConflictException extends Exception

class EmailConflictException extends Exception

class IdConflictException extends Exception

class VerificationFailedException extends Exception

class NoSuchUserException extends Exception
