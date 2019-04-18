package backsapc.healthchecker.checker.dao
import java.util.UUID

import backsapc.healthchecker.checker.domain.Check

import scala.concurrent.Future

trait CheckerRepository {
  def save(check: Check): Future[Check]
  def update(id: UUID, check: Check): Future[Check]
  def delete(id: UUID): Future[Unit]
  def get(id: UUID, userId: UUID): Future[Option[Check]]
  def getAllForUser(userId: UUID): Future[Seq[Check]]
  def getAll(): Future[Seq[Check]]
  def existsWithId(id: UUID): Future[Boolean]
}
