package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import utils.AccountType
import akka.pattern.ask
import akka.util.Timeout
import models.{AccountModelGet, AccountModelPostPut}

import scala.concurrent.duration._
import scala.concurrent.Await

object Account {
  def props(id: BigInt, clientId: BigInt, typeOf: String): Props = Props(new Account(id, clientId, typeOf))

  case class ReplenishAnAccount(requestId: BigInt, value: Int)
  case class WithdrawFromAccount(requestId: BigInt, value: Int)

  case class Acknowledge(id: BigInt, message: String)
  case class NoAcknowledge(id: BigInt, message: String)

  case object GetData
  case class SetData(typeof: String)
}

class Account(id: BigInt, clientId: BigInt, typeOf: String) extends Actor with ActorLogging {
  import Account._

  implicit val timeout: Timeout = Timeout(5 seconds)
//  val clientData: ClientModel = Await.result(
//    client ? Client.GetData,
//    timeout.duration
//  ).asInstanceOf[ClientModel]

  var accountValue: BigInt = 0
  var state: AccountModelGet = AccountModelGet(id, clientId, "", accountValue)
//  var state: AccountModelGet = null

//  override def preStart(): Unit = log.info(s"Account for ${clientData.name} started")
//  override def postStop(): Unit = log.info(s"Account for ${clientData.name} stopped")
  override def preStart(): Unit = log.info(s"Account for $clientId started")
  override def postStop(): Unit = log.info(s"Account for $clientId stopped")

  override def receive: Receive = {
    case ReplenishAnAccount(requestId, value) =>
//      value match {
//        case None =>
//          log.error("No value specified")
//          sender() ! NoAcknowledge(requestId, "No value specified")
//        case Some(optValue) =>
//          accountValue += optValue
//          log.info(s"Account value: $accountValue")
//          sender() ! Acknowledge(requestId, "Successfully replenished")
//      }
      accountValue += value
      log.info(s"Account value: $accountValue")
      state = AccountModelGet(state.id, state.clientId, state.typeOf, accountValue)
      sender() ! Acknowledge(requestId, "Successfully replenished")
    case WithdrawFromAccount(requestId, value) =>
//      value match {
//        case None =>
//          log.error("No value specified")
//          sender() ! NoAcknowledge(requestId, "No value specified")
//        case Some(optValue) =>
//          if (accountValue < optValue){
//            log.error(s"Account value error: ${accountValue - optValue}")
//            sender() ! NoAcknowledge(requestId, s"Value is too much ${accountValue - optValue}")
//          } else {
//            accountValue -= optValue
//            log.info(s"Account value: $accountValue")
//            sender() ! Acknowledge(requestId, "Successfully withdrawal")
//          }
//      }
      if (accountValue < value){
        log.error(s"Account value error: ${accountValue - value}")
        sender() ! NoAcknowledge(requestId, s"Value is too much ${accountValue - value}")
      } else {
        accountValue -= value
        log.info(s"Account value: $accountValue")
        state = AccountModelGet(state.id, state.clientId, state.typeOf, accountValue)
        sender() ! Acknowledge(requestId, "Successfully withdrawal")
      }
    case GetData =>
      log.info("Received GetData request")
//      sender() ! AccountModel(id, clientId, typeOf, accountValue)
//      state = AccountModelGet(state.id, state.clientId, state.typeOf, state.volume)
      sender() ! state

    case SetData(value) =>
      log.info("Received SetData request")
      state = AccountModelGet(state.id, state.clientId, value, state.volume)
      sender() ! state
  }
}
