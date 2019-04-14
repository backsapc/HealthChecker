package backsapc.healthchecker.dao

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

import backsapc.healthchecker.domain.Account
import backsapc.healthchecker.user.bcrypt.BcryptHash

import scala.collection.JavaConverters._
import scala.concurrent.Future

class InMemoryAccountRepository extends AccountRepository {
  private val repo = new ConcurrentHashMap[UUID, Account]()

  override def add(account: Account): Future[Account] = Future successful {
    repo.put(account.id, account)
    account
  }

  override def getById(id: UUID): Future[Option[Account]] = Future.successful(
    repo.asScala.get(id)
  )

  override def getByLogin(login: String): Future[Option[Account]] =
    Future.successful(
      repo.asScala.find(p => p._2.login == login) map (_._2)
    )

  override def updatePassword(id: UUID, password: BcryptHash): Future[Account] =
    Future.successful {
      val account = repo.asScala.getOrElse(id, throw new NoSuchElementException)
      repo.replace(id, account.copy(password = password))
    }

  override def existsWithId(accountId: UUID): Future[Boolean] =
    Future.successful(repo.asScala.contains(accountId))

  override def existsWithLogin(login: String): Future[Boolean] =
    Future.successful(repo.asScala.exists(_._2.login == login))

  override def existsWithEmail(email: String): Future[Boolean] =
    Future.successful(repo.asScala.exists(_._2.email == email))
}
