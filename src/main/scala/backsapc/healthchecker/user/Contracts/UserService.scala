package backsapc.healthchecker.user.Contracts

import java.util.UUID

import backsapc.healthchecker.domain.Account
import backsapc.healthchecker.user.RegisterRequest

import scala.concurrent.Future

trait UserService {
  def register(account: RegisterRequest): Future[Either[String, Account]]

  def update(id: UUID, oldPassword: String, newPassword: String): Future[Either[String, Account]]

  def findById(id: UUID): Future[Option[Account]]

  def findByLogin(login: String): Future[Option[Account]]
}
