package backsapc.healthchecker.user.Contracts

import java.util.UUID

import backsapc.healthchecker.user.Contracts.UserServiceOperationResults._
import backsapc.healthchecker.user.RegisterRequest

import scala.concurrent.Future

object UserServiceOperationResults {
  sealed trait RegisterResult
  final case class RegisterSuccess(account: AccountViewModel) extends RegisterResult
  final case class IdConflict(id: UUID)                       extends RegisterResult
  final case class LoginConflict(login: String)               extends RegisterResult
  final case class EmailConflict(email: String)               extends RegisterResult
  final case class ConfirmationError(email: String)           extends RegisterResult

  sealed trait UpdateResult
  case class UpdateSuccess()                   extends UpdateResult
  case class NoSuchUserError(id: UUID)         extends UpdateResult
  case class InvalidPassword(password: String) extends UpdateResult
}

case class AccountViewModel(id: UUID, login: String, email: String)

trait UserService {
  def register(account: RegisterRequest): Future[RegisterResult]

  def update(
      id: UUID,
      oldPassword: String,
      newPassword: String
  ): Future[UpdateResult]

  def findById(id: UUID): Future[Option[AccountViewModel]]

  def findByLogin(login: String): Future[Option[AccountViewModel]]
}
