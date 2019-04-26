package backsapc.healthchecker.user.dao

import java.util.UUID

import backsapc.healthchecker.user.bcrypt.BcryptHash
import backsapc.healthchecker.user.domain.Account

import scala.concurrent.Future

trait AccountRepository {
  def add(account: Account): Future[Account]
  def delete(id: UUID): Future[Unit]
  def getById(id: UUID): Future[Option[Account]]
  def getByLogin(login: String): Future[Option[Account]]
  def updatePassword(id: UUID, password: BcryptHash): Future[Account]
  def existsWithId(accountId: UUID): Future[Boolean]
  def existsWithLogin(login: String): Future[Boolean]
  def existsWithEmail(email: String): Future[Boolean]
}
