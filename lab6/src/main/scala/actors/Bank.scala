package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object Bank {
  def props(): Props = Props(new Bank)

  case class CreateBankManager(id: Long, name: String)
}

class Bank extends Actor with ActorLogging {
  import Bank._

  override def preStart(): Unit = log.info("Bank Application started")
  override def postStop(): Unit = log.info("Bank Application stopped")

  override def receive: Receive = {
    case CreateBankManager(managerId, managerName) =>
      log.info(s"CreateBankManager with name $managerName received.")
      val bankManager: ActorRef = context.actorOf(BankManager.props(managerId, managerName))
      sender() ! bankManager
  }
}
