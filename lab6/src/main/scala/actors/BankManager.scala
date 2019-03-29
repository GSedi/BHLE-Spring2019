package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout}
import models.{AccountModel, ClientModel}
import utils.{AccountType, ResponseCodes}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._

object BankManager {
  def props(id: Long, name: String): Props = Props(new BankManager(id, name))

  case class Response(status: String, statusCode: Int, message: String, data: String)
//  case class ResponseObject(data: Seq[ClientModel])

  /**
    * Account messages
    */

  case class CreateAccount(accountId: Long, clientId: Long, typeOf: String)
  case object GetAccounts
  case class GetAccount(id: Long)
  case class UpdatedAccount(id: Long, data: String)
  case class CloseAccount(id: Long)
  case class Accounts(accounts: Seq[AccountModel])

  case class ReplenishAnAccount(requestId: Long, accountId: Long, value: Option[Double])
  case class WithdrawFromAccount(requestId: Long, accountId: Long, value: Option[Double])

  /**
    * Client messages
    */

  case class CreateClient(clientId: Long, name: String)
  case object GetClients
  case class GetClient(id: Long)
  case class UpdateClient(id: Long, name: String)
  case class DeleteClient(id: Long)
  case class Clients(clients: Seq[ClientModel])
}

class BankManager(id: Long, name: String) extends Actor with ActorLogging{
  import BankManager._
  override def preStart(): Unit = log.info(s"BankManager $name started")
  override def postStop(): Unit = log.info(s"BankManager $name stopped")

  context.setReceiveTimeout(3 seconds)
  implicit val timeout: Timeout = Timeout(30 seconds)

  var clients = Map.empty[Long, ActorRef]
  var accounts = Map.empty[Long, ActorRef]
  // [accountId, clientId]
  var clientAccounts = Map.empty[Long, Long]

  var removedClients = Map.empty[Long, ActorRef]
  var closedAccounts = Map.empty[Long, ActorRef]

  override def receive: Receive = {
    case CreateClient(clientId, clientName) =>
      log.info(s"CreateClient with name $clientName received.")
      val client: ActorRef = context.actorOf(Client.props(clientId, clientName))
      clients.get(clientId) match {
        case None =>
          clients = clients + (clientId -> client)
          client ! Client.GetData
          context.become(waitingResponse(sender(), "Client created"))
//          sender() ! Response("Ok", ResponseCodes.CREATED , "Client created", )
        case Some(value) =>
          log.error(s"Client with id: $clientId already exist")
          sender() ! Response("Not Ok", ResponseCodes.BAD_REQUEST, s"Client with id: $clientId already exist", "null")
      }

    case GetClients =>
      log.info("Get all clients")
      if (clients.size <= 0){
        sender() ! Response("Ok", ResponseCodes.NO_CONTENT, "No clients specified yet", "null")
      } else {
        clients.values.foreach(clientRef => clientRef ! Client.GetData)
        context.become(waitingResponses(sender(), clients.size, Seq.empty[ClientModel]))
      }

    case GetClient(clientId) =>
      log.info(s"Get client $clientId")
      clients.get(clientId) match {
        case None =>
          log.error(s"Client with clientId: $clientId is not specified")
          sender() ! Response("Not ok", ResponseCodes.NOT_FOUND, "not found", "null")
        case Some(clientRef) =>
          log.info(s"Success for client $clientId")
          clientRef ! Client.GetData
          context.become(waitingResponse(sender(), s"Returned client ${clientId}"))
      }

    case UpdateClient(clientId, clientName) =>
      log.info(s"Update client $clientId")
      clients.get(clientId) match {
        case None =>
          log.error(s"Client with clientId: $clientId is not specified")
        case Some(clientRef) =>
          log.info(s"Success for client $clientId")
          clientRef ! Client.SetData(clientName)
          context.become(waitingResponse(sender(), "Client updated"))
      }

    case DeleteClient(clientId) =>
      log.info(s"Replace client: $clientId to removed")
      clients.get(clientId) match {
        case None =>
          notSpecified(clientId)
          sender() ! Response("Not Ok", ResponseCodes.NO_CONTENT,  "Not specified", "null")
        case Some(value) =>
          removedClients = removedClients + (clientId -> value)
          clients = clients - clientId
          sender() ! Response("Ok", ResponseCodes.SUCCESS, "Client removed", "{}")
      }

    case CreateAccount(accountId, clientId, typeOf) =>
      clients.get(clientId) match {
        case None =>
          log.error(s"Client with clientId: $clientId is not specified")
          sender() ! Response("Not Ok", ResponseCodes.BAD_REQUEST, "Client with clientId: $clientId is not specified", "null")
        case Some(value) =>
          accounts.get(accountId) match {
            case None =>
              log.info(s"Account: $accountId is created")
              val account: ActorRef = context.actorOf(Account.props(accountId, clientId, typeOf))
              accounts = accounts + (accountId -> account)
              clientAccounts.get(accountId) match {
                case None =>
                  clientAccounts = clientAccounts + (clientId -> accountId)
                  account ! Account.GetData
                  context.become(waitingResponse(sender(),  "Account created"))
//                  sender() ! Response("Ok", "Account added")
                case Some(value) =>
                  log.error(s"Account with id: $accountId for client with $clientId already exist")
                  sender() ! Response("Not Ok", ResponseCodes.BAD_REQUEST, s"Account with id: $accountId for client with $clientId already exist", "null")
              }
            case Some(value) =>
              log.error(s"Account with id: $accountId already exist")
              sender() ! Response("Not Ok", ResponseCodes.BAD_REQUEST, s"Client with id: $accountId already exist", "null")
          }
      }

    case GetAccounts =>
      log.info("Get all accounts")
      accounts.values.foreach(accountRef => accountRef ! Account.GetData)
      context.become(waitingResponses(sender(), accounts.size, Seq.empty[AccountModel]))

    case GetAccount(accountId) =>
      log.info(s"Get account $accountId")
      accounts.get(accountId) match {
        case None =>
          log.error(s"Account with accountId: $accountId is not specified")
          sender() ! Response("Not ok", ResponseCodes.NOT_FOUND, "not found", "null")
        case Some(accountRef) =>
          log.info(s"Success for account $accountId")
          accountRef ! Account.GetData
          log.info(s"asvsvasdvasvasvavsd $accountId")
          context.become(waitingResponse(sender(), s"Returned account ${accountId}"))
      }

    case CloseAccount(accountId) =>
      log.info(s"Replace client: $accountId to closed")
      accounts.get(accountId) match {
        case None =>
          notSpecified(accountId)
          sender() ! Response("Not Ok", ResponseCodes.NO_CONTENT,  "Not specified", "null")
        case Some(value) =>
          log.info(s"Replace client: $accountId to closed SUCCESS")
          closedAccounts = closedAccounts + (accountId -> value)
          accounts = accounts - accountId
          sender() ! Response("Ok", ResponseCodes.SUCCESS, "Account closed", "[]")
      }

    case ReplenishAnAccount(requestId, accountId, value) =>
      accounts.get(accountId) match {
        case None => log.error(s"Account with id: $accountId is not specified")
        case Some(accountRef) =>
          log.info(s"Replanish an Account $accountId")
          accountRef ! Account.ReplenishAnAccount(requestId, value)
          context.become(waitingAck(sender()))
      }

    case WithdrawFromAccount(requestId, accountId, value) =>
      accounts.get(accountId) match {
        case None => log.error(s"Account with id: $accountId is not specified")
        case Some(accountRef) =>
          log.info(s"Replanish an Account $accountId")
          accountRef ! Account.WithdrawFromAccount(requestId, value)
          context.become(waitingAck(sender()))
      }
  }

  def waitingResponse(replyTo: ActorRef, message: String): Receive = {
    case clientModel: ClientModel =>
//      replyTo ! ClientModel(clientModel.id, clientModel.name)
      replyTo ! Response("Ok", ResponseCodes.CREATED , message, clientModel.toString)
    case accountModel: AccountModel =>
      replyTo ! Response("Ok", ResponseCodes.CREATED , message, accountModel.toString)
    case ReceiveTimeout =>
      log.error("Received timeout while waiting for Response")
      context.become(receive)
  }

  def waitingResponses(replyTo: ActorRef, responsesLeft: Int, instances: Seq[Any]): Receive = {
    case accountModel: AccountModel =>
      log.info(s"Received AccountModel with id: ${accountModel.id}. Responses left: $responsesLeft")
      if (responsesLeft - 1 <= 0) {
        log.info("All responses received, replying to initial request.")
        replyTo ! Accounts((instances :+ accountModel).asInstanceOf[Seq[AccountModel]])
        context.become(receive)
      }
      else context.become(waitingResponses( replyTo, responsesLeft - 1, instances = instances :+ accountModel))
    case clientModel: ClientModel =>
      log.info(s"Received ClientModel with id: ${clientModel.id} name ${clientModel.name}. Responses left: $responsesLeft")
      if (responsesLeft - 1 <= 0) {
        log.info("All responses received, replying to initial request.")
        replyTo ! Clients((instances :+ clientModel).asInstanceOf[Seq[ClientModel]])
//        replyTo ! Response("Ok", ResponseCodes.SUCCESS, "Success", Clients((instances :+ clientModel).asInstanceOf[Seq[ClientModel]]).toString)
        context.become(receive)
      }
      else context.become(waitingResponses( replyTo, responsesLeft - 1, instances = instances :+ clientModel))
  }

  def waitingAck(replyTo: ActorRef): Receive = {
    case Account.Acknowledge(requestId, message) =>
      replyTo ! Response(requestId + " OK", ResponseCodes.SUCCESS, message, "null")
      context.become(receive)
    case Account.NoAcknowledge(requestId, message) =>
      replyTo ! Response(requestId + " Not OK", ResponseCodes.BAD_REQUEST, message, "null")
      context.become(receive)

    case ReceiveTimeout =>
      log.error("Received timeout while waiting for Ack(s)")
      replyTo ! Response("Not OK", ResponseCodes.REQUEST_TIMEOUT, "Request time out", "null")
      context.become(receive)
  }

  def notSpecified(instanceId: Long): Unit ={
    log.error(s"Instance with id: $instanceId is not specified")
  }

  def success(instanceId: Long): Unit ={
    log.info(s"Instance: $instanceId is created")
  }

}
