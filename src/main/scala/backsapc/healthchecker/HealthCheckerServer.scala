package backsapc.healthchecker

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import backsapc.healthchecker.checker.contracts._
import backsapc.healthchecker.checker.dao.{ InMemoryCheckEventRepository, InMemoryCheckerRepository }
import backsapc.healthchecker.checker.implementation.{
  CheckerServiceImpl,
  HttpCheckerImpl,
  HttpContentCheckerImpl,
  PingCheckerImpl
}
import backsapc.healthchecker.checker.{ CheckJobScheduler, CheckerRouter }
import backsapc.healthchecker.common.Config
import backsapc.healthchecker.notification.NotificationRouter
import backsapc.healthchecker.notification.dao.InMemoryNotificationRepository
import backsapc.healthchecker.notification.implementations.{ MailServiceImpl, NotificationServiceImpl, TokenGenerator }
import backsapc.healthchecker.user.Implementations.{ NotificationClientImpl, TokenServiceImpl, UserServiceImpl }
import backsapc.healthchecker.user.UserRouter
import backsapc.healthchecker.user.bcrypt.AsyncBcryptImpl
import backsapc.healthchecker.user.dao.InMemoryAccountRepository

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success }

object HealthCheckerServer extends App with Config {

  implicit val system: ActorSystem                = ActorSystem("healthAkkaHttpServer")
  implicit val materializer: ActorMaterializer    = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val notificationRepository = new InMemoryNotificationRepository
  val tokenGenerator         = new TokenGenerator
  val mailService            = new MailServiceImpl
  val notificationService    = new NotificationServiceImpl(notificationRepository, tokenGenerator, mailService)
  val notification           = new NotificationRouter(notificationService)

  val accountRepository = new InMemoryAccountRepository
  val bcrypt            = new AsyncBcryptImpl

  val notificationClient = new NotificationClientImpl(notificationService)
  val tokenService       = new TokenServiceImpl(accountRepository, bcrypt)
  val userService        = new UserServiceImpl(accountRepository, bcrypt, notificationClient)
  val user               = new UserRouter(tokenService, userService)

  val checkerJobRepository           = new InMemoryCheckEventRepository
  val checkerRepository              = new InMemoryCheckerRepository
  val checkerHttp                    = new HttpCheckerImpl
  val checkerContent                 = new HttpContentCheckerImpl
  val checkerPing                    = new PingCheckerImpl
  val checkerService: CheckerService = new CheckerServiceImpl(checkerRepository)
  val checker                        = new CheckerRouter(checkerService)
  val checkerNotificationClient =
    new backsapc.healthchecker.checker.implementation.NotificationClientImpl(notificationService)
  val serviceLocator = new ServiceLocator {
    override val httpChecker: HttpChecker               = checkerHttp
    override val httpContentChecker: HttpContentChecker = checkerContent
    override val pingChecker: PingChecker               = checkerPing
    override val notificationClient: NotificationClient = checkerNotificationClient
  }

  val checkJobScheduler = new CheckJobScheduler()

  lazy val routes: Route = user.routes ~ checker.routes ~ notification.routes

  val serverBinding: Future[Http.ServerBinding] =
    Http().bindAndHandle(routes, interface, port)

  serverBinding.onComplete {
    case Success(bound) =>
      checkJobScheduler.run(checkerRepository, checkerJobRepository, serviceLocator)
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
