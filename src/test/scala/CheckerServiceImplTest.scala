import java.time.OffsetDateTime
import java.util.UUID

import backsapc.healthchecker.checker._
import backsapc.healthchecker.checker.dao.CheckerRepository
import backsapc.healthchecker.checker.domain.{ Check, CheckType, CheckViewModel }
import backsapc.healthchecker.checker.implementation.CheckerServiceImpl
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{ AsyncFlatSpec, Matchers }

import scala.concurrent.Future

class CheckerServiceImplTest extends AsyncFlatSpec with AsyncMockFactory with Matchers {
  import backsapc.healthchecker.checker.domain.Mappings._
  val mockCheckerRepository: CheckerRepository = stub[CheckerRepository]

  val checkerService: CheckerServiceImpl =
    new CheckerServiceImpl(mockCheckerRepository)

  val userId: UUID         = UUID.fromString("4485936a-6271-4964-a406-ed1ca9cf194f")
  val checkId: UUID        = UUID.fromString("4485936a-6271-4964-a406-ed1ca9cf195f")
  val interval: Int        = 300
  val friendlyName: String = "some checky boi"
  val url: String          = "https://vk.com"
  val anotherUrl: String   = "https://another.com"
  val content: String      = "content"
  val ip: String           = "127.0.0.1"
  val anotherIp: String    = "127.0.0.2"
  val port: Int            = 8080

  val mockCheck: Check = Check(
    checkId,
    userId,
    friendlyName,
    interval,
    isDeleted = false,
    isPaused = false,
    CheckType.Http,
    Some(url),
    None,
    None,
    None,
    OffsetDateTime.now()
  )

  val mockCreateHttpCheck: CreateHttpCheck =
    CreateHttpCheck(checkId, interval, friendlyName, url)

  val mockUpdateHttpCheck: UpdateHttpCheck =
    UpdateHttpCheck(checkId, interval, friendlyName, url)

  val mockCreateContentCheck: CreateContentCheck =
    CreateContentCheck(checkId, friendlyName, interval, url, content)

  val mockUpdateContentCheck: UpdateContentCheck =
    UpdateContentCheck(checkId, friendlyName, interval, url, content)

  val mockCreatePingCheck: CreatePingCheck =
    CreatePingCheck(checkId, interval, friendlyName, ip, port)

  val mockUpdatePingCheck: UpdatePingCheck =
    UpdatePingCheck(checkId, interval, friendlyName, ip, port)

  behavior of "CheckerServiceImpl"

  "Checker service " should " create http check" in {
    val expected: CheckViewModel = mockCheck
    (mockCheckerRepository.save _).when(*).returns(Future successful mockCheck)
    (mockCheckerRepository.existsWithId _).when(*).returns(Future successful false)
    checkerService.createHttpCheck(mockCreateHttpCheck, userId).map(_ shouldBe expected)
  }

  "Checker service " should " update http check" in {
    val expected: CheckViewModel = mockCheck.copy(url = Some(anotherUrl))
    (mockCheckerRepository.get _).when(*, *).returns(Future successful Some(mockCheck))
    (mockCheckerRepository.update _)
      .when(*, *)
      .returns(Future successful mockCheck.copy(url = Some(anotherUrl)))
    checkerService
      .updateHttpCheck(mockUpdateHttpCheck.copy(url = anotherUrl), userId)
      .map(_ shouldBe expected)
  }

  "Checker service " should " create content check" in {
    val expected: CheckViewModel = mockCheck.copy(content = Some(content))
    (mockCheckerRepository.save _)
      .when(*)
      .returns(Future successful mockCheck.copy(content = Some(content)))
    (mockCheckerRepository.existsWithId _).when(*).returns(Future successful false)
    checkerService
      .createContentCheck(mockCreateContentCheck, userId)
      .map(_ shouldBe expected)
  }

  "Checker service " should " update content check" in {
    val expected: CheckViewModel = mockCheck.copy(url = Some(anotherUrl), content = Some(content))
    (mockCheckerRepository.get _)
      .when(*, *)
      .returns(Future successful Some(mockCheck.copy(content = Some(content))))
    (mockCheckerRepository.update _)
      .when(*, *)
      .returns(Future successful mockCheck.copy(url = Some(anotherUrl), content = Some(content)))
    checkerService
      .updateContentCheck(mockUpdateContentCheck.copy(url = anotherUrl), userId)
      .map(_ shouldBe expected)
  }

  "Checker service " should " create ping check" in {
    val expected: CheckViewModel = mockCheck.copy(ip = Some(ip), port = Some(port))
    (mockCheckerRepository.save _)
      .when(*)
      .returns(Future successful mockCheck.copy(ip = Some(ip), port = Some(port)))
    (mockCheckerRepository.existsWithId _).when(*).returns(Future successful false)
    checkerService
      .createPingCheck(mockCreatePingCheck, userId)
      .map(_ shouldBe expected)
  }

  "Checker service " should " update ping check" in {
    val expected: CheckViewModel = mockCheck.copy(ip = Some(anotherIp), port = Some(port))
    (mockCheckerRepository.get _)
      .when(*, *)
      .returns(Future successful Some(mockCheck.copy(ip = Some(ip), port = Some(port))))
    (mockCheckerRepository.update _)
      .when(*, *)
      .returns(Future successful mockCheck.copy(ip = Some(anotherIp), port = Some(port)))
    checkerService
      .updatePingCheck(mockUpdatePingCheck.copy(ip = anotherIp), userId)
      .map(_ shouldBe expected)
  }
}
