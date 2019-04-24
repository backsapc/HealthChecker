package backsapc.healthchecker.checker.contracts

trait ServiceLocator {
  val httpChecker: HttpChecker
  val httpContentChecker: HttpContentChecker
  val pingChecker: PingChecker
  val notificationClient: NotificationClient
}
