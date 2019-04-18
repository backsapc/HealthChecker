package backsapc.healthchecker.checker.domain
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
  port: Option[Int]
)

object CheckType extends Enumeration {
  val Http = Value("Http")
  val HttpWithContent = Value("Content")
  val Ping = Value("Ping")
}
