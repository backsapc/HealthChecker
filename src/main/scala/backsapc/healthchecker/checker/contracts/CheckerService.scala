package backsapc.healthchecker.checker.contracts
import java.util.UUID

import backsapc.healthchecker.checker._
import backsapc.healthchecker.checker.domain.Check

import scala.concurrent.Future

trait CheckerService {
  def getAll(): Future[Seq[Check]]
  def getById(id: UUID, userId: UUID): Future[Option[Check]]
  def getAllForUserId(userId: UUID): Future[Seq[Check]]
  def delete(id: UUID, userId: UUID): Future[Unit]
  def pause(id: UUID, userId: UUID): Future[Unit]
  def activate(id: UUID, userId: UUID): Future[Unit]
  def createHttpCheck(createHttpCheck: CreateHttpCheck, userId: UUID): Future[Check]
  def updateHttpCheck( updateHttpCheck: UpdateHttpCheck, userId: UUID): Future[Check]
  def createContentCheck(createContentCheck: CreateContentCheck, userId: UUID): Future[Check]
  def updateContentCheck(updateContentCheck: UpdateContentCheck, userId: UUID): Future[Check]
  def createPingCheck(createPingCheck: CreatePingCheck, userId: UUID): Future[Check]
  def updatePingCheck(updatePingCheck: UpdatePingCheck, userId: UUID): Future[Check]
}
