package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout}
import utils.AccountType
import akka.util.Timeout
import models.AccountModelGet

import scala.concurrent.duration._

object Account {
  def props(id: BigInt, clientId: BigInt, typeOf: String): Props = Props(new Account(id, clientId, typeOf))

  case class ReplenishAnAccount(requestId: BigInt, value: BigInt)
  case class WithdrawFromAccount(requestId: BigInt, value: BigInt)

  case class BalanceTransfer(requestId: BigInt, fAccountId: BigInt, tAccountId: BigInt, value: BigInt, accounts: Map[BigInt, ActorRef], initSender: ActorRef)

  case class Acknowledge(id: BigInt, message: String)
  case class NoAcknowledge(id: BigInt, message: String)

  case object GetData
  case class SetData(typeof: String)
}

class Account(id: BigInt, clientId: BigInt, typeOf: String) extends Actor with ActorLogging {
  import Account._

  implicit val timeout: Timeout = Timeout(5 seconds)

  var accountValue: BigInt = 0
  var state: AccountModelGet = AccountModelGet(id, clientId, "", accountValue)
  var ok: Boolean = true

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
      if(id == fAccountId){
        log.info(s"VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV")
        accounts(fAccountId) ! Account.WithdrawFromAccount(requestId, value)
        context.become(waitingAck(initSender,accounts(fAccountId),fAccountId, tAccountId, value, accounts))

//        accounts.get(tAccountId) match {
//          case None =>
//            log.info(s"Account $tAccountId does not exist")
//            initSender ! NoAcknowledge(requestId, s"Account $tAccountId does not exist")
//          case Some(tAccountRef) =>
//            log.info(s"Account $tAccountId does exist")
//            accounts.get(fAccountId) match {
//              case None =>
//                log.info(s"Account $fAccountId does not exist")
//                initSender ! NoAcknowledge(requestId, s"Account $fAccountId does not exist")
//              case Some(fAccountRef) =>
//                log.info(s"Account $fAccountId  exist")
//                fAccountRef ! Account.WithdrawFromAccount(requestId, value)
//                context.become(waitingAck(initSender,tAccountRef,fAccountId, tAccountId, value, accounts))
//
//            }

//        }

      }
      if(id == tAccountId){

        accounts(tAccountId) ! Account.ReplenishAnAccount(requestId, value)
        context.become(waitingAck(initSender, accounts(tAccountId),0, 0, value, accounts))

//        accounts.get(fAccountId) match {
//          case None =>
//            log.info(s"Account $fAccountId does not exist")
//            initSender ! NoAcknowledge(requestId, s"Account $fAccountId does not exist")
//          case Some(fAccountRef) =>
//            log.info(s"Account $fAccountId exist")
//            accounts.get(tAccountId) match {
//              case None =>
//                log.info(s"Account $tAccountId does not exist")
//                initSender ! NoAcknowledge(requestId, s"Account $tAccountId does not exist")
//              case Some(tAccountRef) =>
//                log.info(s"Account $tAccountId exist")
//                tAccountRef ! Account.ReplenishAnAccount(requestId, value)
//                context.become(waitingAck(initSender, fAccountRef,0, 0, value, accounts))
//
//            }

        }
        if (fAccountId == 0 && tAccountId == 0){
          initSender ! Acknowledge(requestId, s"$value succefull transfered")
        }
        if(!ok){
          initSender ! NoAcknowledge(requestId, "Something get wrong")
        }


    case GetData =>
      log.info("Received GetData request")
      sender() ! state

    case SetData(value) =>
      log.info("Received SetData request")
      state = AccountModelGet(state.id, state.clientId, value, state.balance)
      sender() ! state
  }

  def waitingAck(initSender: ActorRef,replyTo: ActorRef, fAccountId: BigInt, tAccountId: BigInt, value: BigInt, accounts: Map[BigInt, ActorRef]): Receive = {
    case Account.Acknowledge(requestId, message) =>
      log.info(s"Account value: ascasdcascasdcadsc")
      replyTo ! BalanceTransfer(requestId, fAccountId, tAccountId, value, accounts, initSender)
      context.become(receive)

    case Account.NoAcknowledge(requestId, message) =>
      log.info(s"Account AAAAAAAAAAAAAAAAAAAAA: ascasdcascasdcadsc")
      ok = false
      replyTo ! BalanceTransfer(requestId, fAccountId, tAccountId, value, accounts, initSender)
      context.become(receive)

  }
}
