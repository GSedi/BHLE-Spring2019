package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout}
import models.{AccountModelGet, ClientModelGet}
import utils.{AccountType, StatusCodes}
import akka.util.Timeout
import scala.concurrent.duration._

/*
ALL PAST DATA IN GIT "LAB6 REFACTORING" COMMIT
 */

object BankManager {
  def props(id: BigInt, name: String): Props = Props(new BankManager(id, name))

  case class Response(statusCode: Int, message: String)
  case class StatusInfo(statusCode: Int, message: String)

  /**
    * Account messages
    */

  case class CreateAccount(clientId: BigInt, typeOf: String)
  case object GetAccounts
  case class GetAccount(id: BigInt)
  case class UpdatedAccount(id: BigInt, typeOf: String)
  case class CloseAccount(id: BigInt)
  case class Accounts(accounts: Seq[AccountModelGet])

  case class ReplenishAnAccount(accountId: BigInt, value: BigInt)
  case class WithdrawFromAccount(accountId: BigInt, value: BigInt)

  case class BalanceTransfer(fAccountId: BigInt, tAccountId: BigInt, value: BigInt, initSender: ActorRef)

  /**
    * Client messages
    */

  case class CreateClient(firstName: String, lastName: String, birthday: String)
  case object GetClients
  case class GetClient(id: BigInt)
  case class UpdateClient(id: BigInt, firstName: String, lastName: String, birthday: String)
  case class DeleteClient(id: BigInt)
  case class Clients(clients: Seq[ClientModelGet])
}

class BankManager(id: BigInt, name: String) extends Actor with ActorLogging{
  import BankManager._
  override def preStart(): Unit = log.info(s"BankManager $name started")
  override def postStop(): Unit = log.info(s"BankManager $name stopped")

  context.setReceiveTimeout(30 seconds)
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
    case CreateClient(clientFirstName, clientLastName, birthday) =>
      log.info(s"CreateClient with firstName $clientFirstName received.")
      cntClientId += 1
      val client: ActorRef = context.actorOf(Client.props(cntClientId))
      clients = clients + (cntClientId -> client)
      client ! Client.SetData(clientFirstName, clientLastName, birthday)
      context.become(waitingResponse(sender()))

    case GetClients =>
      log.info("Get all clients")
      if (clients.size <= 0){
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

        case Some(clientRef) =>
          log.info(s"Success for client $clientId")
          clientRef ! Client.GetData
          context.become(waitingResponse(sender()))
      }

    case UpdateClient(clientId, clientFirstName, clientLastName, birthday) =>
      log.info(s"Update client $clientId")
      clients.get(clientId) match {
        case None =>
          log.error(s"Client with clientId: $clientId is not specified")
          sender() ! Left(StatusInfo(StatusCodes.NOT_FOUND, s"Client with clientId: $clientId is not found"))

        case Some(clientRef) =>
          log.info(s"Success for client $clientId")
          clientRef ! Client.SetData(clientFirstName, clientLastName, birthday)
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
          var accountIds = Seq.empty[BigInt]
          clientAccounts.foreach(v => {
              if (v._2 == clientId){
                accountIds = accountIds :+ v._1
              }
          })
          accounts.foreach(v => {
            accountIds.foreach(i => {
              if (i == v._1){
                closedAccounts = closedAccounts + (v._1 -> v._2)
              }
            })
          })
          accountIds.foreach(i => {
            accounts = accounts - i
          })
          sender() ! Right(StatusInfo(StatusCodes.SUCCESS, "Client removed"))
      }

    case CreateAccount(clientId, typeOf) =>
      clients.get(clientId) match {
        case None =>
          log.error(s"Client with clientId: $clientId is not specified")
          sender() ! Left(StatusInfo(StatusCodes.NOT_FOUND, s"Client with clientId: $clientId is not specified"))

        case Some(value) =>
          cntAccountId += 1
          log.info(s"Account: $cntAccountId is created")
          val account: ActorRef = context.actorOf(Account.props(cntAccountId, clientId, typeOf))
          accounts = accounts + (cntAccountId -> account)
          clientAccounts = clientAccounts + (cntAccountId -> clientId)
          account ! Account.SetData(typeOf)
          context.become(waitingResponse(sender()))
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

    case BalanceTransfer(fAccountId, tAccountId, value, initSender) =>
      accounts.get(fAccountId) match {
        case None =>
          log.error(s"Account with id: $fAccountId is not specified")
          sender() ! Left(StatusInfo(StatusCodes.NOT_FOUND, s"Account with accountId: $fAccountId is not specified"))

        case Some(fAccountRef) =>
          accounts.get(tAccountId) match {
            case None =>
              log.error(s"Account with id: $fAccountId is not specified")
            case Some(tAccountRef) =>
              cntRequestId += 1
              log.info(s"Transfer balance from $fAccountId to $tAccountId")
              tAccountRef ! Account.BalanceTransfer(cntRequestId, fAccountId, tAccountId, value, accounts, initSender)
              context.become(waitingAck(sender()))
          }
      }
  }

  def waitingResponse(replyTo: ActorRef): Receive = {
    case clientModel: ClientModelGet =>
      replyTo ! Right(clientModel)
      context.become(receive)

    case accountModel: AccountModelGet =>
      replyTo ! Right(accountModel)
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
      log.info(s"Received ClientModel with id: ${clientModel.id} firstName ${clientModel.firstName}. Responses left: $responsesLeft")
      if (responsesLeft - 1 <= 0) {
        log.info("All responses received, replying to initial request.")
        replyTo ! Right(Clients((instances :+ clientModel).asInstanceOf[Seq[ClientModelGet]]))
        context.become(receive)
      }
      else context.become(waitingResponses( replyTo, responsesLeft - 1, instances = instances :+ clientModel))

    case ReceiveTimeout =>
      log.error("Received timeout while waiting for Response")
      replyTo ! Left(StatusInfo(StatusCodes.REQUEST_TIMEOUT, "Recieved timeout exception"))
      context.become(receive)
  }

  def waitingAck(replyTo: ActorRef): Receive = {
    case Account.Acknowledge(requestId, message) =>
      replyTo ! Right(StatusInfo(StatusCodes.SUCCESS, message))
      context.become(receive)

    case Account.NoAcknowledge(requestId, message) =>
      replyTo ! Right(StatusInfo(StatusCodes.BAD_REQUEST, message))
      context.become(receive)

    case ReceiveTimeout =>
      log.error("Received timeout while waiting for Ack(s)")
      replyTo ! Left(StatusInfo(StatusCodes.REQUEST_TIMEOUT, "Request time out"))
      context.become(receive)
  }

}
