package backsapc.healthchecker.checker.dao

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

import backsapc.healthchecker.checker.domain.Check

import scala.collection.JavaConverters._
import scala.concurrent.Future

class InMemoryCheckerRepository extends CheckerRepository {
  private val repo = new ConcurrentHashMap[UUID, Check]()

  override def save(check: Check): Future[Check] =
    Future successful repo.put(check.id, check)

  override def update(id: UUID, check: Check): Future[Check] = Future successful {
    val toUpdate = repo.asScala.getOrElse(id, throw new NoSuchElementException)
    repo.replace(id, check)
  }

  override def delete(id: UUID): Future[Unit] = Future successful {
    val toDelete = repo.asScala.getOrElse(id, throw new NoSuchElementException)
    repo.asScala.replace(id, toDelete.copy(isDeleted = true))
  }

  override def get(id: UUID, userId: UUID): Future[Option[Check]] =
    Future successful repo.asScala.find(check => check._1 == id && check._2.userId == userId).map(_._2)

  override def getAllForUser(userId: UUID): Future[Seq[Check]] =
    Future successful repo.asScala.filter(_._2.userId == userId).values.toSeq

  override def getAll(): Future[Seq[Check]] =
    Future successful repo.asScala.values.toSeq

  override def existsWithId(id: UUID): Future[Boolean] = Future successful repo.containsKey(id)
}
