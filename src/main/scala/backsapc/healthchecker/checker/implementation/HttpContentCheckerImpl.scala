package backsapc.healthchecker.checker.implementation

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import backsapc.healthchecker.checker.contracts.HttpContentChecker
import backsapc.healthchecker.checker.domain.HttpContentCheckModel
import backsapc.healthchecker.checker.implementation.RichHttpClient.HttpClient
import backsapc.healthchecker.checker.jobs.{ CheckResult, FailedCheckResult, SuccessCheckResult }

import scala.concurrent.{ ExecutionContextExecutor, Future }

class HttpContentCheckerImpl(implicit actorSystem: ActorSystem, materializer: ActorMaterializer)
    extends HttpContentChecker {
  implicit val dispatcher: ExecutionContextExecutor             = actorSystem.dispatcher
  private val simpleClient: HttpRequest => Future[HttpResponse] = Http().singleRequest(_: HttpRequest)
  private val redirectingClient: HttpClient                     = RichHttpClient.httpClientWithRedirect(simpleClient)

  override def doCheck(
      check: HttpContentCheckModel
  ): Future[CheckResult] =
    redirectingClient(
      HttpRequest(
        uri = Uri(check.url),
        method = HttpMethods.GET
      )
    ).map(
        response => {
          response.status match {
            case StatusCodes.OK ⇒
              if (response.entity.toString.contains(check.content)) SuccessCheckResult(check.id)
              else FailedCheckResult(check.id, s"${check.content} was not in request response.")
            case _ => FailedCheckResult(check.id, s"Status code ${response.status} does not indicate success.")
          }
        }
      )
      .recover {
        case error => FailedCheckResult(check.id, s"Your ${check.url} is not available.")
      }

}
