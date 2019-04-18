package backsapc.healthchecker.user

//#json-support
import backsapc.healthchecker.common.JsonSupport
import backsapc.healthchecker.user.Contracts.TokenServiceOperationResults.{
  GenerateResult,
  WrongPasswordError
}
import backsapc.healthchecker.user.Contracts.UserServiceOperationResults._
import backsapc.healthchecker.user.Contracts.{
  AccountViewModel,
  TokenServiceOperationResults
}
import backsapc.healthchecker.user.domain.Account

trait UserJsonSupport extends JsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import spray.json.DefaultJsonProtocol._

  implicit val accountJsonFormat = jsonFormat4(Account)
  implicit val loginRequestJsonFormat = jsonFormat2(LoginRequest)
  implicit val registerRequestJsonFormat = jsonFormat4(RegisterRequest)
  implicit val accountViewModelJsonFormat = jsonFormat3(AccountViewModel)
  implicit val loginConflictJsonFormat = jsonFormat1(LoginConflict)
  implicit val idConflictJsonFormat = jsonFormat1(IdConflict)
  implicit val emailConflictJsonFormat = jsonFormat1(EmailConflict)
  implicit val registerResult = jsonFormat0(() => new RegisterResult)

  implicit val wrongPasswordJsonFormat = jsonFormat1(WrongPasswordError)
  implicit val noSuchUserJsonFormat = jsonFormat1(
    TokenServiceOperationResults.NoSuchUserError
  )
  implicit val generateSuccessJsonFormat = jsonFormat1(
    TokenServiceOperationResults.GenerateSuccess
  )
  implicit val generateResult = jsonFormat0(() => new GenerateResult)

  implicit val wrongPasswordUpdateJsonFormat = jsonFormat1(InvalidPassword)
  implicit val noSuchUserUpdateJsonFormat = jsonFormat1(NoSuchUserError)
  implicit val updateSuccessJsonFormat = jsonFormat0(UpdateSuccess)
  implicit val updateResult = jsonFormat0(() => new UpdateResult)
}
