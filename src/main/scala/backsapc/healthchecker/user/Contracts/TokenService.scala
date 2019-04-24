package backsapc.healthchecker.user.Contracts

import backsapc.healthchecker.user.Contracts.TokenServiceOperationResults.GenerateResult
import backsapc.healthchecker.user.LoginRequest

import scala.concurrent.Future

object TokenServiceOperationResults {
  sealed trait GenerateResult
  final case class GenerateSuccess(token: String)       extends GenerateResult
  final case class WrongPasswordError(password: String) extends GenerateResult
  final case class NoSuchUserError(login: String)       extends GenerateResult
}

trait TokenService {
  def generate(request: LoginRequest): Future[GenerateResult]
}
