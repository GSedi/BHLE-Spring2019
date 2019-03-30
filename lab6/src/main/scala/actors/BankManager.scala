package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout}
import models.{AccountModelGet, ClientModelGet, Model}
import utils.{AccountType, StatusCodes}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.Random

object BankManager {
  def props(id: BigInt, name: String): Props = Props(new BankManager(id, name))

//  case class Response[T <: Model](status: String, statusCode: Int, message: String, data: T)
  case class Response(statusCode: Int, message: String)
  case class StatusInfo(statusCode: Int, message: String)
//  case class ResponseObject(data: Seq[ClientModel])

  /**
    * Account messages
    */

//  case class CreateAccount(accountId: BigInt, clientId: BigInt, typeOf: String)
  case class CreateAccount(clientId: BigInt, typeOf: String)
  case object GetAccounts
  case class GetAccount(id: BigInt)
  case class UpdatedAccount(id: BigInt, typeOf: String)
  case class CloseAccount(id: BigInt)
  case class Accounts(accounts: Seq[AccountModelGet])

  case class ReplenishAnAccount(accountId: BigInt, value: Int)
  case class WithdrawFromAccount(accountId: BigInt, value: Int)

  /**
    * Client messages
    */

//  case class CreateClient(clientId: BigInt, name: String)
  case class CreateClient(name: String)
  case object GetClients
  case class GetClient(id: BigInt)
  case class UpdateClient(id: BigInt, name: String)
  case class DeleteClient(id: BigInt)
  case class Clients(clients: Seq[ClientModelGet])
}

class BankManager(id: BigInt, name: String) extends Actor with ActorLogging{
  import BankManager._
  override def preStart(): Unit = log.info(s"BankManager $name started")
  override def postStop(): Unit = log.info(s"BankManager $name stopped")

  context.setReceiveTimeout(3 seconds)
  implicit val timeout: Timeout = Timeout(30 seconds)

  var clients = Map.empty[BigInt, ActorRef]
  var accounts = Map.empty[BigInt, ActorRef]
  // [accountId, clientId]
  var clientAccounts = Map.empty[BigInt, BigInt]

  var removedClients = Map.empty[BigInt, ActorRef]
  var closedAccounts = Map.empty[BigInt, ActorRef]

  var cntClientId: BigInt = 0
  var cntAccountId: BigInt = 0
  var cntRequestId: BigInt = 0

  override def receive: Receive = {
    case CreateClient(clientName) =>
      log.info(s"CreateClient with name $clientName received.")
      cntClientId += 1
      val client: ActorRef = context.actorOf(Client.props(cntClientId))
      clients = clients + (cntClientId -> client)
      client ! Client.SetData(clientName)
      context.become(waitingResponse(sender()))
////      val client: ActorRef = context.actorOf(Client.props(clientId, clientName))
////        val client: ActorRef = context.actorOf(Client.props(clientId))
//      clients.get(clientId) match {
//        case None =>
//          cntClientId += 1
//          val client: ActorRef = context.actorOf(Client.props(cntClientId))
//          clients = clients + (clientId -> client)
//          client ! Client.SetData(clientName)
////          client ! Client.GetData
//          context.become(waitingResponse(sender()))
////          sender() ! Response("Ok", StatusCodes.CREATED , "Client created", )
//        case Some(value) =>
//          log.error(s"Client with id: $clientId already exist")
//          sender() ! Left(StatusInfo(StatusCodes.DUPLICATE_ENTITY, s"Client with id: $clientId already exist"))
////          sender() ! Response("Not Ok", StatusCodes.BAD_REQUEST, s"Client with id: $clientId already exist", "null")
//      }

    case GetClients =>
      log.info("Get all clients")
      if (clients.size <= 0){
//        sender() ! Response("Ok", StatusCodes.NO_CONTENT, "No clients specified yet", "null")
        sender() ! Right(Clients(Seq.empty[ClientModelGet]))
      } else {
        clients.values.foreach(clientRef => clientRef ! Client.GetData)
        context.become(waitingResponses(sender(), clients.size, Seq.empty[ClientModelGet]))
      }

    case GetClient(clientId) =>
      log.info(s"Get client $clientId")
      clients.get(clientId) match {
        case None =>
          log.error(s"Client with clientId: $clientId is not specified")
          sender() ! Left(StatusInfo(StatusCodes.NOT_FOUND, s"Client with clientId: $clientId is not found"))
//          sender() ! Response("Not ok", StatusCodes.NOT_FOUND, "not found", "null")
        case Some(clientRef) =>
          log.info(s"Success for client $clientId")
          clientRef ! Client.GetData
          context.become(waitingResponse(sender()))
      }

    case UpdateClient(clientId, clientName) =>
      log.info(s"Update client $clientId")
      clients.get(clientId) match {
        case None =>
          log.error(s"Client with clientId: $clientId is not specified")
          sender() ! Left(StatusInfo(StatusCodes.NOT_FOUND, s"Client with clientId: $clientId is not found"))
        case Some(clientRef) =>
          log.info(s"Success for client $clientId")
          clientRef ! Client.SetData(clientName)
          context.become(waitingResponse(sender()))
      }

    case DeleteClient(clientId) =>
      log.info(s"Replace client: $clientId to removed")
      clients.get(clientId) match {
        case None =>
          sender() ! Left(StatusInfo(StatusCodes.NOT_FOUND, s"Client with clientId: $clientId is not found"))
        case Some(value) =>
          removedClients = removedClients + (clientId -> value)
          clients = clients - clientId
          sender() ! Right(StatusInfo(StatusCodes.SUCCESS, "Client removed"))
      }

    case CreateAccount(clientId, typeOf) =>
      clients.get(clientId) match {
        case None =>
          log.error(s"Client with clientId: $clientId is not specified")
//          sender() ! Response("Not Ok", StatusCodes.BAD_REQUEST, "Client with clientId: $clientId is not specified", "null")
          sender() ! Left(StatusInfo(StatusCodes.NOT_FOUND, s"Client with clientId: $clientId is not specified"))
        case Some(value) =>
          cntAccountId += 1
          log.info(s"Account: $cntAccountId is created")
          val account: ActorRef = context.actorOf(Account.props(cntAccountId, clientId, typeOf))
          accounts = accounts + (cntAccountId -> account)
          clientAccounts = clientAccounts + (cntAccountId -> clientId)
          account ! Account.SetData(typeOf)
          context.become(waitingResponse(sender()))
//          accounts.get(accountId) match {
//            case None =>
//              log.info(s"Account: $accountId is created")
//              val account: ActorRef = context.actorOf(Account.props(accountId, clientId, typeOf))
//              accounts = accounts + (accountId -> account)
//              clientAccounts.get(accountId) match {
//                case None =>
//                  clientAccounts = clientAccounts + (clientId -> accountId)
//                  account ! Account.GetData
//                  context.become(waitingResponse(sender()))
////                  sender() ! Response("Ok", "Account added")
//                case Some(value) =>
//                  log.error(s"Account with id: $accountId for client with $clientId already exist")
//                  sender() ! Response("Not Ok", StatusCodes.BAD_REQUEST, s"Account with id: $accountId for client with $clientId already exist", "null")
//              }
//            case Some(value) =>
//              log.error(s"Account with id: $accountId already exist")
//              sender() ! Response("Not Ok", StatusCodes.BAD_REQUEST, s"Client with id: $accountId already exist", "null")
//          }
      }

    case GetAccounts =>
      if(accounts.size <= 0){
        sender() ! Right(Accounts(Seq.empty[AccountModelGet]))
      } else {
        log.info("Get all accounts")
        accounts.values.foreach(accountRef => accountRef ! Account.GetData)
        context.become(waitingResponses(sender(), accounts.size, Seq.empty[AccountModelGet]))
      }

    case GetAccount(accountId) =>
      log.info(s"Get account $accountId")
      accounts.get(accountId) match {
        case None =>
          log.error(s"Account with accountId: $accountId is not specified")
          sender() ! Left(StatusInfo(StatusCodes.NOT_FOUND, s"Account with accountId: $accountId is not specified"))
        case Some(accountRef) =>
          log.info(s"Success for account $accountId")
          accountRef ! Account.GetData
          context.become(waitingResponse(sender()))
      }

    case UpdatedAccount(accountId, typeOf) =>
      log.info(s"Update account $accountId")
      accounts.get(accountId) match {
        case None =>
          log.error(s"Account with accountId: $accountId is not specified")
          sender() ! Left(StatusInfo(StatusCodes.NOT_FOUND, s"Account with accountId: $accountId is not specified"))
        case Some(accountRef) =>
          log.info(s"Success for account $accountId")
          accountRef ! Account.SetData(typeOf)
          context.become(waitingResponse(sender()))
      }

    case CloseAccount(accountId) =>
      log.info(s"Replace client: $accountId to closed")
      accounts.get(accountId) match {
        case None =>
          sender() ! Left(StatusInfo(StatusCodes.NOT_FOUND, s"Account with accountId: $accountId is not specified"))
        case Some(value) =>
          log.info(s"Replace client: $accountId to closed SUCCESS")
          closedAccounts = closedAccounts + (accountId -> value)
          accounts = accounts - accountId
          sender() ! Right(StatusInfo(StatusCodes.SUCCESS, s"Account $accountId closed"))
      }

    case ReplenishAnAccount(accountId, value) =>
      accounts.get(accountId) match {
        case None =>
          log.error(s"Account with id: $accountId is not specified")
          sender() ! Left(StatusInfo(StatusCodes.NOT_FOUND, s"Account with accountId: $accountId is not specified"))
        case Some(accountRef) =>
          cntRequestId += 1
          log.info(s"Replanish an Account $accountId")
          accountRef ! Account.ReplenishAnAccount(cntRequestId, value)
          context.become(waitingAck(sender()))
      }

    case WithdrawFromAccount(accountId, value) =>
      accounts.get(accountId) match {
        case None =>
          log.error(s"Account with id: $accountId is not specified")
          sender() ! Left(StatusInfo(StatusCodes.NOT_FOUND, s"Account with accountId: $accountId is not specified"))
        case Some(accountRef) =>
          cntRequestId += 1
          log.info(s"Replanish an Account $accountId")
          accountRef ! Account.WithdrawFromAccount(cntRequestId, value)
          context.become(waitingAck(sender()))
      }
  }

  def waitingResponse(replyTo: ActorRef): Receive = {
    case clientModel: ClientModelGet =>
//      replyTo ! ClientModel(clientModel.id, clientModel.name)
//      replyTo ! Response("Ok", StatusCodes.CREATED , message, clientModel.toString)
      replyTo ! Right(clientModel)
      context.become(receive)
    case accountModel: AccountModelGet =>
      replyTo ! Right(accountModel)
//      replyTo ! Response("Ok", StatusCodes.CREATED , message, accountModel.toString)
//      replyTo ! Response("Ok", StatusCodes.CREATED , message, accountModel)
      context.become(receive)
    case ReceiveTimeout =>
      log.error("Received timeout while waiting for Response")
      replyTo ! Left(StatusInfo(StatusCodes.REQUEST_TIMEOUT, "Recieved timeout exception"))
      context.become(receive)
  }

  def waitingResponses(replyTo: ActorRef, responsesLeft: Int, instances: Seq[Any]): Receive = {
    case accountModel: AccountModelGet =>
      log.info(s"Received AccountModel with id: ${accountModel.id}. Responses left: $responsesLeft")
      if (responsesLeft - 1 <= 0) {
        log.info("All responses received, replying to initial request.")
        replyTo ! Right(Accounts((instances :+ accountModel).asInstanceOf[Seq[AccountModelGet]]))
        context.become(receive)
      }
      else context.become(waitingResponses( replyTo, responsesLeft - 1, instances = instances :+ accountModel))
    case clientModel: ClientModelGet =>
      log.info(s"Received ClientModel with id: ${clientModel.id} name ${clientModel.name}. Responses left: $responsesLeft")
      if (responsesLeft - 1 <= 0) {
        log.info("All responses received, replying to initial request.")
        replyTo ! Right(Clients((instances :+ clientModel).asInstanceOf[Seq[ClientModelGet]]))
//        replyTo ! Response("Ok", StatusCodes.SUCCESS, "Success", Clients((instances :+ clientModel).asInstanceOf[Seq[ClientModel]]).toString)
        context.become(receive)
      }
      else context.become(waitingResponses( replyTo, responsesLeft - 1, instances = instances :+ clientModel))
  }

  def waitingAck(replyTo: ActorRef): Receive = {
    case Account.Acknowledge(requestId, message) =>
//      replyTo ! Response(requestId + " OK", StatusCodes.SUCCESS, message, "null")
      replyTo ! Right(StatusInfo(StatusCodes.SUCCESS, message))
      context.become(receive)
    case Account.NoAcknowledge(requestId, message) =>
//      replyTo ! Response(requestId + " Not OK", StatusCodes.BAD_REQUEST, message, "null")
      replyTo ! Right(StatusInfo(StatusCodes.SUCCESS, message))
      context.become(receive)

    case ReceiveTimeout =>
      log.error("Received timeout while waiting for Ack(s)")
//      replyTo ! Response("Not OK", StatusCodes.REQUEST_TIMEOUT, "Request time out", "null")
      replyTo ! Left(StatusInfo(StatusCodes.REQUEST_TIMEOUT, "Request time out"))
      context.become(receive)
  }

}
