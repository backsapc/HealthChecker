package backsapc.healthchecker.common

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import authentikat.jwt.JsonWebToken

class JwtService extends Config {
  def authenticated: Directive1[Map[String, Any]] =
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(jwt) if isTokenExpired(jwt) =>
        complete(StatusCodes.Unauthorized -> "Token expired.")

      case Some(jwt) if JsonWebToken.validate(jwt, secretKey) =>
        provide(getClaims(jwt).getOrElse(Map.empty[String, Any]))

      case _ => complete(StatusCodes.Unauthorized)
    }

  def getLogin(claims: Map[String, Any]): Option[String] = claims.get("login").map(_.toString)

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
