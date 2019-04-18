package backsapc.healthchecker.checker.implementation

import java.util.UUID

import backsapc.healthchecker.checker._
import backsapc.healthchecker.checker.contracts.CheckerService
import backsapc.healthchecker.checker.dao.CheckerRepository
import backsapc.healthchecker.checker.domain.{Check, CheckType}
import backsapc.healthchecker.common.Config
import io.lemonlabs.uri.parsing.UriParsingException
import io.lemonlabs.uri.{AbsoluteUrl, IpV4}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class CheckerServiceImpl(val repository: CheckerRepository)(
  implicit executionContext: ExecutionContext
) extends Config
    with CheckerService {

  override def getAll(): Future[Seq[Check]] =
    repository
      .getAll()
      .map(
        _.filter(_.isDeleted == false)
          .map(replacePunycode)
      )

  override def getById(id: UUID, userId: UUID): Future[Option[Check]] =
    repository
      .get(id, userId)
      .map(
        _.filter(_.isDeleted == false)
          .map(replacePunycode)
      )

  override def getAllForUserId(userId: UUID): Future[Seq[Check]] =
    repository
      .getAllForUser(userId)
      .map(
        _.filter(_.isDeleted == false)
          .map(replacePunycode)
      )

  override def delete(id: UUID, userId: UUID): Future[Unit] =
    updateCheck(id, userId, _.copy(isDeleted = true)).map(_ => ())

  override def pause(id: UUID, userId: UUID): Future[Unit] =
    updateCheck(id, userId, _.copy(isPaused = true)).map(_ => ())

  override def activate(id: UUID, userId: UUID): Future[Unit] =
    updateCheck(id, userId, _.copy(isPaused = false)).map(_ => ())

  override def createHttpCheck(createHttpCheck: CreateHttpCheck, userId: UUID): Future[Check] = {
    val url = parseUri(createHttpCheck.url)
    validateInterval(createHttpCheck.interval)

    checkForCollisionAndSave(
      createHttpCheck.id,
      Check(
        id = createHttpCheck.id,
        userId = userId,
        friendlyName = createHttpCheck.friendlyName,
        interval = createHttpCheck.interval,
        isDeleted = false,
        isPaused = false,
        checkType = CheckType.Http,
        url = Some(url.toStringPunycode),
        content = None,
        ip = None,
        port = None
      )
    )
  }

  override def updateHttpCheck(updateHttpCheck: UpdateHttpCheck, userId: UUID): Future[Check] = {
    val url: AbsoluteUrl = parseUri(updateHttpCheck.url)
    validateInterval(updateHttpCheck.interval)

    updateCheck(
      updateHttpCheck.id,
      userId,
      _.copy(
        url = Some(url.toStringPunycode),
        interval = updateHttpCheck.interval,
        friendlyName = updateHttpCheck.friendlyName
      )
    )
  }

  override def createContentCheck(
    createContentCheck: CreateContentCheck,
    userId: UUID
  ): Future[Check] = {
    val url: AbsoluteUrl = parseUri(createContentCheck.url)
    validateInterval(createContentCheck.interval)

    checkForCollisionAndSave(
      createContentCheck.id,
      Check(
        id = createContentCheck.id,
        userId = userId,
        friendlyName = createContentCheck.friendlyName,
        interval = createContentCheck.interval,
        isDeleted = false,
        isPaused = false,
        checkType = CheckType.HttpWithContent,
        url = Some(url.toStringPunycode),
        content = Some(createContentCheck.content),
        ip = None,
        port = None
      )
    )
  }

  override def updateContentCheck(
    updateContentCheck: UpdateContentCheck,
    userId: UUID
  ): Future[Check] = {
    val url: AbsoluteUrl = parseUri(updateContentCheck.url)
    validateInterval(updateContentCheck.interval)

    updateCheck(
      updateContentCheck.id,
      userId,
      _.copy(
        url = Some(url.toStringPunycode),
        content = Some(updateContentCheck.content),
        interval = updateContentCheck.interval,
        friendlyName = updateContentCheck.friendlyName
      )
    )
  }

  override def createPingCheck(createPingCheck: CreatePingCheck, userId: UUID): Future[Check] = {
    val ip = parseIpV4(createPingCheck.ip)
    validatePort(createPingCheck.port)

    checkForCollisionAndSave(
      createPingCheck.id,
      Check(
        createPingCheck.id,
        userId,
        createPingCheck.friendlyName,
        createPingCheck.interval,
        isDeleted = false,
        isPaused = false,
        checkType = CheckType.Ping,
        url = None,
        content = None,
        ip = Some(ip.toString),
        port = Some(createPingCheck.port)
      )
    )
  }

  override def updatePingCheck(updatePingCheck: UpdatePingCheck, userId: UUID): Future[Check] = {
    val ip = parseIpV4(updatePingCheck.ip)
    validatePort(updatePingCheck.port)

    updateCheck(
      updatePingCheck.id,
      userId,
      _.copy(
        ip = Some(ip.toString),
        port = Some(updatePingCheck.port),
        interval = updatePingCheck.interval,
        friendlyName = updatePingCheck.friendlyName
      )
    )
  }

  private def updateCheck(checkId: UUID, userId: UUID, modifier: Check => Check): Future[Check] =
    repository.get(checkId, userId).flatMap {
      case Some(check) =>
        if (check.isDeleted)
          Future failed new NoSuchElementException
        else if (check.userId == userId)
          repository.update(checkId, modifier(check))
        else
          Future failed new InvalidUserIdException(userId)
      case None => Future failed new NoSuchElementException
    }

  private def checkForCollisionAndSave(id: UUID, check: Check): Future[Check] =
    repository.existsWithId(id).flatMap {
      if (_)
        Future.failed(new CheckIdCollisionException(id))
      else
        repository.save(check)
    }

  private def replacePunycode(check: Check): Check =
    if (check.url.isDefined) check.copy(url = Some(AbsoluteUrl.parse(check.url.get).toString))
    else check

  private def validateInterval(
    interval: Int
  ): Unit = {
    if (minCheckInterval > interval || interval > maxCheckInterval) {
      throw new CheckIntervalOutOfBoundsException(
        provided = interval,
        minCheckInterval,
        maxCheckInterval
      )
    }
  }

  private def parseUri(url: String): AbsoluteUrl = AbsoluteUrl.parseTry(url) match {
    case Success(value) => value
    case Failure(_: UriParsingException) =>
      throw new MalformedUrlException(url)
    case Failure(exception) =>
      throw exception
  }

  private def parseIpV4(ip: String): IpV4 = IpV4.parseTry(ip) match {
    case Success(value) => value
    case Failure(_: UriParsingException) =>
      throw new MalformedIpException(ip)
    case Failure(exception) =>
      throw exception
  }

  private def validatePort(
    port: Int
  ): Unit = {
    if (port < minimumPortNumber || port > maximumPortNumber) {
      throw new PortOutOfBoundsException(
        port,
        min = minimumPortNumber,
        max = maximumPortNumber
      )
    }
  }
}

class InvalidUserIdException(id: UUID) extends Exception
class CheckIdCollisionException(id: UUID) extends Exception
class CheckIntervalOutOfBoundsException(provided: Int, min: Int, max: Int) extends Exception
class PortOutOfBoundsException(provided: Int, min: Int, max: Int) extends Exception
class MalformedUrlException(provided: String) extends Exception
class MalformedIpException(provided: String) extends Exception
