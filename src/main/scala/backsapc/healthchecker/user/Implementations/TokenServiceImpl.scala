package backsapc.healthchecker.user.Implementations

import java.util.concurrent.TimeUnit

import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import backsapc.healthchecker.common.Config
import backsapc.healthchecker.dao.AccountRepository
import backsapc.healthchecker.user.Contracts.TokenService
import backsapc.healthchecker.user.LoginRequest

import scala.concurrent.{ExecutionContext, Future}

class TokenServiceImpl(repository: AccountRepository)(implicit executionContext: ExecutionContext) extends Config with TokenService {

  private val expires = tokenExpiryPeriodInDays
  private val key = secretKey
  private val header = JwtHeader(headerType)

  def generate(request: LoginRequest): Future[Either[String, String]] =
    repository.getByLogin(request.login).map {
      case Some(account) if account.password == request.password =>
        val claims = setClaims(request.login, tokenExpiryPeriodInDays)
        Right(JsonWebToken(header, claims, secretKey))
      case Some(_) => Left("Wrong password")
      case None => Left("No such user")
    }

  private def setClaims(username: String, expiryPeriodInDays: Long) =
    JwtClaimsSet(Map("user" -> username,
      "expiredAt" -> (System.currentTimeMillis() + TimeUnit.DAYS
        .toMillis(expiryPeriodInDays)))
    )
}
