package backsapc.healthchecker.checker.dao
import java.time.OffsetDateTime
import java.util.UUID

import backsapc.healthchecker.checker.domain.{ Check, CheckEvent }

import scala.concurrent.Future

trait CheckerRepository {
  def save(check: Check): Future[Check]
  def update(id: UUID, check: Check): Future[Check]
  def delete(id: UUID): Future[Unit]
  def get(id: UUID, userId: UUID): Future[Option[Check]]
  def getByIds(ids: Seq[UUID]): Future[Seq[Check]]
  def getAllForUser(userId: UUID): Future[Seq[Check]]
  def getAll(): Future[Seq[Check]]
  def existsWithId(id: UUID): Future[Boolean]
  def getOutdatedChecks(offsetDateTime: OffsetDateTime): Future[Seq[Check]]
  def updateCheckLastCheckDate(ids: Seq[UUID]): Future[Unit]
}

trait CheckerJobRepository {
  def createCheckEvents(checkIds: Seq[UUID]): Future[Seq[CheckEvent]]
  def notStartedExist(): Future[Boolean]
  def getNotStartedCheckEvents(count: Int): Future[Seq[CheckEvent]]
  def updateCheckEventsAsStarted(ids: Seq[Long]): Future[Seq[CheckEvent]]
  def updateCheckEventAsCompleted(id: Long, isSuccessful: Boolean, message: String): Future[Unit]
}
