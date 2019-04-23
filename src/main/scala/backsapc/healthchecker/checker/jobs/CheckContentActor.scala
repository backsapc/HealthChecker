package backsapc.healthchecker.checker.jobs
import akka.actor.{ Actor, Props }
import backsapc.healthchecker.checker.contracts.HttpContentChecker
import backsapc.healthchecker.checker.domain.HttpContentCheckModel

class CheckContentActor(httpChecker: HttpContentChecker) extends Actor {
  import CheckContentActor._
  import akka.pattern.pipe
  import context.dispatcher

  def receive = {
    case DoHttpContentCheck(check) => httpChecker.doCheck(check).pipeTo(sender()); ()
  }
}

object CheckContentActor {
  case class DoHttpContentCheck(check: HttpContentCheckModel)

  def props(httpChecker: HttpContentChecker): Props = Props(new CheckContentActor(httpChecker))
}
