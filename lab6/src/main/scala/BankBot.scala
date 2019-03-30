import actors.{Bank, BankManager}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout

import scala.concurrent.duration._
import akka.pattern.ask
import utils.Debit

import scala.concurrent.{Await, Future}

object BankBot {
  def props(): Props = Props(new BankBot)
  case object StartBank
}

class BankBot extends Actor with ActorLogging{
  import BankBot._

  val bank = context.actorOf(Bank.props(), "bank-app")

  override def preStart(): Unit = log.info("BankBot started")
  override def postStop(): Unit = log.info("BankBot stopped")

  override def receive: Receive = {
    case StartBank =>
      log.info(s"StartBank received.")
      implicit val timeout: Timeout = Timeout(5 seconds)
      val result = bank ? Bank.CreateBankManager(1, "admin")
      val admin = Await.result(result, timeout.duration).asInstanceOf[ActorRef]
      sender() ! admin
  }

}
