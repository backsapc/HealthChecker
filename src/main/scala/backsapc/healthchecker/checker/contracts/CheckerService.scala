package backsapc.healthchecker.checker.contracts
import java.util.UUID

import backsapc.healthchecker.checker._
import backsapc.healthchecker.checker.domain.CheckViewModel

import scala.concurrent.Future

trait CheckerService {
  def getAll(): Future[Seq[CheckViewModel]]
  def getById(id: UUID, userId: UUID): Future[Option[CheckViewModel]]
  def getAllForUserId(userId: UUID): Future[Seq[CheckViewModel]]
  def delete(id: UUID, userId: UUID): Future[Unit]
  def pause(id: UUID, userId: UUID): Future[Unit]
  def activate(id: UUID, userId: UUID): Future[Unit]
  def createHttpCheck(createHttpCheck: CreateHttpCheck, userId: UUID): Future[CheckViewModel]
  def updateHttpCheck(updateHttpCheck: UpdateHttpCheck, userId: UUID): Future[CheckViewModel]
  def createContentCheck(createContentCheck: CreateContentCheck, userId: UUID): Future[CheckViewModel]
  def updateContentCheck(updateContentCheck: UpdateContentCheck, userId: UUID): Future[CheckViewModel]
  def createPingCheck(createPingCheck: CreatePingCheck, userId: UUID): Future[CheckViewModel]
  def updatePingCheck(updatePingCheck: UpdatePingCheck, userId: UUID): Future[CheckViewModel]
}
