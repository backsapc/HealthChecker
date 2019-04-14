package backsapc.healthchecker.user.Implementations

import java.util.UUID
import java.util.concurrent.TimeUnit

import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import backsapc.healthchecker.common.Config
import backsapc.healthchecker.dao.AccountRepository
import backsapc.healthchecker.user.Contracts.TokenService
import backsapc.healthchecker.user.Contracts.TokenServiceOperationResults.{GenerateResult, GenerateSuccess, NoSuchUserError, WrongPasswordError}
import backsapc.healthchecker.user.LoginRequest
import backsapc.healthchecker.user.bcrypt.AsyncBcrypt

import scala.concurrent.{ExecutionContext, Future}


class TokenServiceImpl(repository: AccountRepository, bCrypt: AsyncBcrypt)(implicit executionContext: ExecutionContext) extends Config with TokenService {

  private val expires = tokenExpiryPeriodInDays
  private val key = secretKey
  private val header = JwtHeader(headerType)

  def generate(request: LoginRequest): Future[GenerateResult] =
    repository.getByLogin(request.login).flatMap {
      case Some(account) => bCrypt.verify(request.password, account.password).map {
        case true =>
          val claims = setClaims(account.id, request.login, tokenExpiryPeriodInDays)
          GenerateSuccess(JsonWebToken(header, claims, secretKey))
        case false => WrongPasswordError(request.password)
      }
      case None => Future successful NoSuchUserError(request.login)
    }

  private def setClaims(id: UUID, username: String, expiryPeriodInDays: Long) =
    JwtClaimsSet(Map("user" -> username, "id" -> id,
      "expiredAt" -> (System.currentTimeMillis() + TimeUnit.DAYS
        .toMillis(expiryPeriodInDays)))
    )
}
