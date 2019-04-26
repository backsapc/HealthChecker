package backsapc.healthchecker.checker.jobs
import akka.actor.{ Actor, Props }
import backsapc.healthchecker.checker.contracts.PingChecker
import backsapc.healthchecker.checker.domain.PingCheckModel

class CheckPingActor(pingChecker: PingChecker) extends Actor {
  import CheckPingActor._
  import akka.pattern.pipe
  import context.dispatcher

  def receive = {
    case DoPingCheck(check) => pingChecker.doCheck(check).pipeTo(sender()); ()
  }
}

object CheckPingActor {
  case class DoPingCheck(check: PingCheckModel)

  def props(pingChecker: PingChecker): Props = Props(new CheckPingActor(pingChecker))
}
