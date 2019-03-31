package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout}
import utils.AccountType
import akka.util.Timeout
import models.AccountModelGet

import scala.collection.mutable
import scala.collection.mutable.Stack
import scala.concurrent.duration._

object Account {
  def props(id: BigInt, clientId: BigInt, typeOf: String): Props = Props(new Account(id, clientId, typeOf))

  sealed trait AccountMessage

  case class ReplenishAnAccount(requestId: BigInt, value: BigInt) extends AccountMessage
  case class WithdrawFromAccount(requestId: BigInt, value: BigInt) extends AccountMessage

  case class BalanceTransfer(requestId: BigInt, fAccountId: BigInt, tAccountId: BigInt, value: BigInt, accounts: Map[BigInt, ActorRef], initSender: ActorRef) extends AccountMessage

  case class Acknowledge(id: BigInt, message: String) extends AccountMessage
  case class NoAcknowledge(id: BigInt, message: String) extends AccountMessage

  case object GetData extends AccountMessage
  case class SetData(typeof: String) extends AccountMessage
}

class Account(id: BigInt, clientId: BigInt, typeOf: String) extends Actor with ActorLogging {
  import Account._

  implicit val timeout: Timeout = Timeout(5 seconds)

  var accountValue: BigInt = 0
  var state: AccountModelGet = AccountModelGet(id, clientId, "", accountValue)
  var ok: Boolean = true
  var levels = Stack[(ActorRef, AccountMessage)]()

  override def preStart(): Unit = log.info(s"Account for $clientId started")
  override def postStop(): Unit = log.info(s"Account for $clientId stopped")

  override def receive: Receive = {
    case ReplenishAnAccount(requestId, value) =>
      accountValue += value
      log.info(s"Account value: $accountValue")
      state = AccountModelGet(state.id, state.clientId, state.typeOf, accountValue)
      sender() ! Acknowledge(requestId, "Successfully replenished")

    case WithdrawFromAccount(requestId, value) =>
      if (accountValue < value){
        log.error(s"Account value error: ${accountValue - value}")
        sender() ! NoAcknowledge(requestId, s"Value is too much ${accountValue - value}")
      } else {
        accountValue -= value
        log.info(s"Account value: $accountValue")
        state = AccountModelGet(state.id, state.clientId, state.typeOf, accountValue)
        sender() ! Acknowledge(requestId, "Successfully withdrawal")
      }

    case BalanceTransfer(requestId, fAccountId, tAccountId, value, accounts, initSender) =>
      log.info(s"Recieved BalanceTransfer request ${id}")

      if(!ok){
//        initSender ! NoAcknowledge(requestId, "Something get wrong")

      } else if(id == tAccountId){
        val fref: ActorRef = accounts(fAccountId)
        //        tref ! ReplenishAnAccount(requestId, value)
        fref ! WithdrawFromAccount(requestId, value)
//        levels.push(fref -> ReplenishAnAccount(requestId, value))
        context.become(waitingAck(initSender, fref,fAccountId, tAccountId, value, accounts, ReplenishAnAccount(requestId, value)))

      } else if(id == fAccountId){
        log.info(s"VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV")
        val tref: ActorRef = accounts(tAccountId)
//        fref ! WithdrawFromAccount(requestId, value)
        tref ! ReplenishAnAccount(requestId, value)
//        levels.push(tref -> WithdrawFromAccount(requestId, value))
        context.become(waitingAck(initSender, tref, fAccountId, tAccountId, value, accounts,  WithdrawFromAccount(requestId, value)))

      }


    case GetData =>
      log.info("Received GetData request")
      sender() ! state

    case SetData(value) =>
      log.info("Received SetData request")
      state = AccountModelGet(state.id, state.clientId, value, state.balance)
      sender() ! state
  }

  def waitingAck(initSender: ActorRef,replyTo: ActorRef, fAccountId: BigInt, tAccountId: BigInt, value: BigInt, accounts: Map[BigInt, ActorRef], accMessage: AccountMessage): Receive = {
    case Acknowledge(requestId, message) =>
      log.info(s"$replyTo and $accMessage")
      levels.push((replyTo, accMessage))
      log.info(s"Levels value: ${levels.size}")
      if (levels.size == 2){
        initSender ! Acknowledge(requestId, s"$value succefull transfered")
        levels.clear()
      } else {
        replyTo ! BalanceTransfer(requestId, fAccountId, tAccountId, value, accounts, initSender)
      }
      context.become(receive)

    case NoAcknowledge(requestId, message) =>
      log.info(s"Account AAAAAAAAAAAAAAAAAAAAA: ascasdcascasdcadsc")
      ok = false
      while(levels.nonEmpty){
        val inst: (ActorRef, AccountMessage) = levels.pop()
//        inst._1 ! inst._2
        log.info(s"AIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIccount ${inst._2}")
        inst._1.tell(inst._2, null)
      }
//      replyTo ! BalanceTransfer(requestId, fAccountId, tAccountId, value, accounts, initSender)
      initSender ! NoAcknowledge(requestId, message)
      ok = true
      context.become(receive)

  }
}
