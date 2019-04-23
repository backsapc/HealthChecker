package backsapc.healthchecker.checker.implementation

import java.net.InetAddress

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import backsapc.healthchecker.checker.contracts.PingChecker
import backsapc.healthchecker.checker.domain.PingCheckModel
import backsapc.healthchecker.checker.implementation.RichHttpClient.HttpClient
import backsapc.healthchecker.checker.jobs.{ CheckResult, FailedCheckResult, SuccessCheckResult }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContextExecutor, Future }

class PingCheckerImpl(implicit actorSystem: ActorSystem, materializer: ActorMaterializer) extends PingChecker {
  implicit val dispatcher: ExecutionContextExecutor             = actorSystem.dispatcher
  private val simpleClient: HttpRequest => Future[HttpResponse] = Http().singleRequest(_: HttpRequest)
  private val redirectingClient: HttpClient                     = RichHttpClient.httpClientWithRedirect(simpleClient)

  private val pingWaitTimeout = 2.seconds.toMillis.toInt

  override def doCheck(
      check: PingCheckModel
  ): Future[CheckResult] = Future {
    val address = InetAddress.getByName(check.ip)
    if (address.isReachable(pingWaitTimeout)) SuccessCheckResult(check.id)
    else FailedCheckResult(check.id, s"${check.ip} is unreachable")
  }

}
