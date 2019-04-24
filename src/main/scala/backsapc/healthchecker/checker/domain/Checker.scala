package backsapc.healthchecker.checker.domain
import java.time.OffsetDateTime
import java.util.UUID

case class Check(
    id: UUID,
    userId: UUID,
    friendlyName: String,
    interval: Int,
    isDeleted: Boolean,
    isPaused: Boolean,
    checkType: CheckType.Value,
    url: Option[String],
    content: Option[String],
    ip: Option[String],
    port: Option[Int],
    lastCheck: OffsetDateTime
)

case class CheckViewModel(
    id: UUID,
    userId: UUID,
    friendlyName: String,
    interval: Int,
    isPaused: Boolean,
    checkType: CheckType.Value,
    url: Option[String],
    content: Option[String],
    ip: Option[String],
    port: Option[Int]
)

case class CheckEvent(
    id: Long,
    checkId: UUID,
    createdAt: OffsetDateTime,
    status: CheckEventStatus.Value,
    message: Option[String]
)

object CheckEventStatus extends Enumeration {
  val NotStarted = Value("NotStarted")
  val Started    = Value("Started")
  val Successful = Value("Successful")
  val Failed     = Value("Failed")
}

object CheckType extends Enumeration {
  val Http            = Value("Http")
  val HttpWithContent = Value("Content")
  val Ping            = Value("Ping")
}

case class HttpCheckModel(
    id: UUID,
    userId: UUID,
    url: String
)

case class HttpContentCheckModel(
    id: UUID,
    userId: UUID,
    url: String,
    content: String
)

case class PingCheckModel(
    id: UUID,
    userId: UUID,
    ip: String,
    port: Int
)

object Mappings {
  import scala.language.implicitConversions
  implicit def checkToCheckModel(check: Check): CheckViewModel = {
    println(check)
    CheckViewModel(
      id = check.id,
      userId = check.userId,
      friendlyName = check.friendlyName,
      interval = check.interval,
      isPaused = check.isPaused,
      checkType = check.checkType,
      url = check.url,
      content = check.content,
      ip = check.ip,
      port = check.port
    )
  }
}
