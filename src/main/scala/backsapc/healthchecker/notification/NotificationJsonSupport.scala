package backsapc.healthchecker.notification
import backsapc.healthchecker.common.JsonSupport

trait NotificationJsonSupport extends JsonSupport {
  import spray.json.DefaultJsonProtocol._

  implicit val confirmationErrorResultJsonFormat = jsonFormat1(ConfirmationErrorResult)
  implicit val confirmationSuccessResultJsonFormat = jsonFormat1(ConfirmationSuccessResult)
}
