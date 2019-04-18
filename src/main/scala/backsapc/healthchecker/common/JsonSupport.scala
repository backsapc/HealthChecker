package backsapc.healthchecker.common

//#json-support
import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import backsapc.healthchecker.user.bcrypt.BcryptHash
import spray.json.{DeserializationException, JsString, JsValue, JsonFormat}

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)

  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)

    def read(value: JsValue): UUID = {
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _ =>
          throw DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }

  implicit object BrcyptHashFormat extends JsonFormat[BcryptHash] {
    def write(m: BcryptHash) = JsString(s"${m.hash}")

    def read(json: JsValue): BcryptHash = json match {
      case JsString(s) => BcryptHash(s)
      case _           => throw DeserializationException("String expected")
    }
  }
}
