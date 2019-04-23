package backsapc.healthchecker.checker.implementation
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import backsapc.healthchecker.checker.contracts.HttpChecker
import backsapc.healthchecker.checker.domain.HttpCheckModel
import backsapc.healthchecker.checker.implementation.RichHttpClient.HttpClient
import backsapc.healthchecker.checker.jobs.{ CheckResult, FailedCheckResult, SuccessCheckResult }

import scala.concurrent.{ ExecutionContextExecutor, Future }

class HttpCheckerImpl(implicit actorSystem: ActorSystem, materializer: ActorMaterializer) extends HttpChecker {
  implicit val dispatcher: ExecutionContextExecutor             = actorSystem.dispatcher
  private val simpleClient: HttpRequest => Future[HttpResponse] = Http().singleRequest(_: HttpRequest)
  private val redirectingClient: HttpClient                     = RichHttpClient.httpClientWithRedirect(simpleClient)

  override def doCheck(
      check: HttpCheckModel
  ): Future[CheckResult] =
    redirectingClient(
      HttpRequest(
        uri = Uri(check.url),
        method = HttpMethods.GET
      )
    ).map(
      response => {
        response.entity.discardBytes()
        response.status match {
          case StatusCodes.OK ⇒ SuccessCheckResult(check.id)
          case _              => FailedCheckResult(check.id, s"Status code ${response.status} does not indicate success.")
        }
      }
    )

}
