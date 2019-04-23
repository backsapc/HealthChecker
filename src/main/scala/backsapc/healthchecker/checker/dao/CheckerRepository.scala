package backsapc.healthchecker.checker.dao
import java.time.OffsetDateTime
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
  def getOutdatedChecks(offsetDateTime: OffsetDateTime): Future[Seq[Check]]
  def markChecksToInProgress(ids: Seq[UUID]): Future[Unit]
}

trait CheckerJobRepository {
  def getOutdatedChecks(count: Int): Future[Seq[Check]]
  def updateCheckLastUpdate(id: UUID): Future[Unit]
  def updateChecksLastUpdate(ids: Seq[UUID]): Future[Unit]
}
