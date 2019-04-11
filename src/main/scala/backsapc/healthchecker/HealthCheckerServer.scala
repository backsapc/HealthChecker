package backsapc.healthchecker

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import backsapc.healthchecker.dao.InMemoryAccountRepository
import backsapc.healthchecker.user.Implementations.{TokenServiceImpl, UserServiceImpl}
import backsapc.healthchecker.user.UserRouter
import backsapc.healthchecker.user.bcrypt.AsyncBcryptImpl

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object HealthCheckerServer extends App {

  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val accountRepository = new InMemoryAccountRepository

  val bcrypt = new AsyncBcryptImpl

  val tokenService = new TokenServiceImpl(accountRepository, bcrypt)
  val userService = new UserServiceImpl(accountRepository, bcrypt)
  val user = new UserRouter(tokenService, userService)

  lazy val routes = user.routes

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "0.0.0.0", 8080)

  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}