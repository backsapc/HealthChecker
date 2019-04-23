package backsapc.healthchecker.checker

//#json-support
import backsapc.healthchecker.checker.domain.{ CheckType, CheckViewModel }
import backsapc.healthchecker.common.JsonSupport

trait CheckerJsonSupport extends JsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import spray.json.DefaultJsonProtocol._

  implicit val enumConverter                = new EnumJsonConverter(CheckType)
  implicit val checkJsonFormat              = jsonFormat10(CheckViewModel)
  implicit val createHttpCheckJsonFormat    = jsonFormat4(CreateHttpCheck)
  implicit val updateHttpCheckJsonFormat    = jsonFormat4(UpdateHttpCheck)
  implicit val createContentCheckJsonFormat = jsonFormat5(CreateContentCheck)
  implicit val updateContentCheckJsonFormat = jsonFormat5(UpdateContentCheck)
  implicit val createPingCheckJsonFormat    = jsonFormat5(CreatePingCheck)
  implicit val updatePingCheckJsonFormat    = jsonFormat5(UpdatePingCheck)
}
