package backsapc.healthchecker.common

import com.typesafe.config.ConfigFactory

trait Config {
  protected val config     = ConfigFactory.load()
  protected val interface  = config.getString("http.interface")
  protected val port       = config.getInt("http.port")
  protected val dbUrl      = config.getString("db.url")
  protected val dbUser     = config.getString("db.user")
  protected val dbPassword = config.getString("db.password")

  protected val domain = config.getString("web.domain")

  protected val tokenExpiryPeriodInDays =
    config.getInt("auth.tokenExpiryPeriodInDays")
  protected val secretKey  = config.getString("auth.secretKey")
  protected val headerType = config.getString("auth.jwtHeaderType")

  protected val minCheckInterval = config.getInt("check.minimumCheckIntervalInSeconds")
  protected val maxCheckInterval = config.getInt("check.maximumCheckIntervalInSeconds")

  protected val maximumPortNumber = config.getInt("check.maximumPortNumber")
  protected val minimumPortNumber = config.getInt("check.minimumPortNumber")

  protected val emailLogin      = config.getString("email.login")
  protected val emailPassword   = config.getString("email.password")
  protected val emailServer     = config.getString("email.server")
  protected val emailServerPort = config.getInt("email.port")
}
