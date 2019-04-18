package backsapc.healthchecker.common

import java.util.UUID

import akka.http.scaladsl.model.headers.HttpChallenges
import akka.http.scaladsl.server.AuthenticationFailedRejection.CredentialsRejected
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{
  AuthenticationFailedRejection,
  AuthorizationFailedRejection,
  Directive1
}
import authentikat.jwt.JsonWebToken

class JwtService extends Config {
  val authorizationHeader = "Authorization"

  def authenticated: Directive1[Map[String, Any]] =
    optionalHeaderValueByName(authorizationHeader).flatMap {
      case Some(jwt) if isTokenExpired(jwt) =>
        reject(
          AuthenticationFailedRejection(
            CredentialsRejected,
            HttpChallenges.oAuth2(jwt)
          )
        )

      case Some(jwt) if JsonWebToken.validate(jwt, secretKey) =>
        provide(getClaims(jwt).getOrElse(Map.empty[String, Any]))

      case Some(jwt) =>
        reject(
          AuthenticationFailedRejection(
            CredentialsRejected,
            HttpChallenges.oAuth2(jwt)
          )
        )

      case _ => reject(AuthorizationFailedRejection)
    }

  def getLogin(claims: Map[String, Any]): String =
    claims.get("user").map(_.toString).get

  def getId(claims: Map[String, Any]): UUID =
    claims.get("id").map(id => UUID.fromString(id.toString)).get

  private def getClaims(jwt: String): Option[Map[String, String]] = jwt match {
    case JsonWebToken(_, claims, _) => claims.asSimpleMap.toOption
    case _                          => None
  }

  private def isTokenExpired(jwt: String) = getClaims(jwt) match {
    case Some(claims) =>
      claims.get("expiredAt") match {
        case Some(value) => value.toLong < System.currentTimeMillis()
        case None        => false
      }
    case None => false
  }
}
