package backsapc.healthchecker

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import backsapc.healthchecker.checker.contracts.CheckerService
import backsapc.healthchecker.checker.dao.InMemoryCheckerRepository
import backsapc.healthchecker.checker.implementation.{
  CheckerServiceImpl,
  HttpCheckerImpl,
  HttpContentCheckerImpl,
  PingCheckerImpl
}
import backsapc.healthchecker.checker.{ CheckJobScheduler, CheckerRouter }
import backsapc.healthchecker.user.Implementations.{ TokenServiceImpl, UserServiceImpl }
import backsapc.healthchecker.user.UserRouter
import backsapc.healthchecker.user.bcrypt.AsyncBcryptImpl
import backsapc.healthchecker.user.dao.InMemoryAccountRepository

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success }

object HealthCheckerServer extends App {

  implicit val system: ActorSystem                = ActorSystem("healthAkkaHttpServer")
  implicit val materializer: ActorMaterializer    = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val accountRepository = new InMemoryAccountRepository

  val bcrypt = new AsyncBcryptImpl

  val tokenService = new TokenServiceImpl(accountRepository, bcrypt)
  val userService  = new UserServiceImpl(accountRepository, bcrypt)
  val user         = new UserRouter(tokenService, userService)

  val checkerRepository              = new InMemoryCheckerRepository
  val checkerHttp                    = new HttpCheckerImpl
  val checkerContent                 = new HttpContentCheckerImpl
  val checkerPing                    = new PingCheckerImpl
  val checkerService: CheckerService = new CheckerServiceImpl(checkerRepository)
  val checker                        = new CheckerRouter(checkerService)

  val checkJobScheduler = new CheckJobScheduler()

  lazy val routes: Route = user.routes ~ checker.routes

  val serverBinding: Future[Http.ServerBinding] =
    Http().bindAndHandle(routes, "0.0.0.0", 8080)

  serverBinding.onComplete {
    case Success(bound) =>
      checkJobScheduler.run(checkerRepository, checkerRepository, checkerHttp, checkerContent, checkerPing)
      println(
        s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/"
      )
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
