package backsapc.healthchecker.user.Contracts

import backsapc.healthchecker.user.LoginRequest

import scala.concurrent.Future

trait TokenService {
  def generate(request: LoginRequest): Future[Either[String, String]]
}
