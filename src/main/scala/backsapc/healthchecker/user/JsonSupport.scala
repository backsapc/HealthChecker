package backsapc.healthchecker.user

//#json-support
import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import backsapc.healthchecker.domain.Account
import spray.json.{DeserializationException, JsString, JsValue, JsonFormat}

trait JsonSupport extends SprayJsonSupport{
  // import the default encoders for primitive types (Int, String, Lists etc)
  import spray.json.DefaultJsonProtocol._
  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)
    def read(value: JsValue): UUID = {
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _              => throw DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }

  implicit val accountJsonFormat = jsonFormat4(Account)
  implicit val loginRequest = jsonFormat2(LoginRequest)
  implicit val registerRequest = jsonFormat4(RegisterRequest)
}
