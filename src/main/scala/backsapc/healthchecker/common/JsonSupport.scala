package backsapc.healthchecker.common

//#json-support
import java.text.{DateFormat, SimpleDateFormat}
import java.util.{Date, UUID}

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import backsapc.healthchecker.user.bcrypt.BcryptHash
import spray.json.{DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)

  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)

    def read(value: JsValue): UUID =
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _ =>
          throw DeserializationException("Expected hexadecimal UUID string")
      }
  }

  implicit object BrcyptHashFormat extends JsonFormat[BcryptHash] {
    def write(m: BcryptHash) = JsString(s"${m.hash}")

    def read(json: JsValue): BcryptHash = json match {
      case JsString(s) => BcryptHash(s)
      case _           => throw DeserializationException("String expected")
    }
  }

  implicit object JavaDateFormat extends JsonFormat[Date] {
    def write(m: Date) = JsString(s"${m.toString}")

    def read(json: JsValue): Date = json match {
      case JsString(s) => new SimpleDateFormat().parse(s)
      case _           => throw DeserializationException("String expected")
    }
  }

  class EnumJsonConverter[T <: scala.Enumeration](enu: T) extends RootJsonFormat[T#Value] {
    override def write(obj: T#Value): JsValue = JsString(obj.toString)

    override def read(json: JsValue): T#Value =
      json match {
        case JsString(txt) => enu.withName(txt)
        case somethingElse =>
          throw DeserializationException(
            s"Expected a value from enum $enu instead of $somethingElse"
          )
      }
  }

}
