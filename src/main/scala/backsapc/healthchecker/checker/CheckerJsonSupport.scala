package backsapc.healthchecker.checker

//#json-support
import backsapc.healthchecker.checker.domain.{Check, CheckType}
import backsapc.healthchecker.common.JsonSupport
import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

trait CheckerJsonSupport extends JsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import spray.json.DefaultJsonProtocol._

  class EnumJsonConverter[T <: scala.Enumeration](enu: T)
      extends RootJsonFormat[T#Value] {
    override def write(obj: T#Value): JsValue = JsString(obj.toString)

    override def read(json: JsValue): T#Value = {
      json match {
        case JsString(txt) => enu.withName(txt)
        case somethingElse =>
          throw DeserializationException(
            s"Expected a value from enum $enu instead of $somethingElse"
          )
      }
    }
  }

  implicit val enumConverter = new EnumJsonConverter(CheckType)
  implicit val checkJsonFormat = jsonFormat11(Check)
  implicit val createHttpCheckJsonFormat = jsonFormat4(CreateHttpCheck)
  implicit val updateHttpCheckJsonFormat = jsonFormat4(UpdateHttpCheck)
  implicit val createContentCheckJsonFormat = jsonFormat5(CreateContentCheck)
  implicit val updateContentCheckJsonFormat = jsonFormat5(UpdateContentCheck)
  implicit val createPingCheckJsonFormat = jsonFormat5(CreatePingCheck)
  implicit val updatePingCheckJsonFormat = jsonFormat5(UpdatePingCheck)
}
