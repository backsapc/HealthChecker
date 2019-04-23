package backsapc.healthchecker.checker.jobs
import akka.actor.{ Actor, Props }
import backsapc.healthchecker.checker.contracts.HttpChecker
import backsapc.healthchecker.checker.domain.HttpCheckModel

class CheckHttpActor(httpChecker: HttpChecker) extends Actor {
  import CheckHttpActor._
  import akka.pattern.pipe
  import context.dispatcher

  def receive = {
    case DoHttpCheck(check) => httpChecker.doCheck(check).pipeTo(sender()); ()
  }
}

object CheckHttpActor {
  case class DoHttpCheck(check: HttpCheckModel)

  def props(httpChecker: HttpChecker): Props = Props(new CheckHttpActor(httpChecker))
}
