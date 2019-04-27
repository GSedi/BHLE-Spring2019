package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import models.{AccountModel, ClientModel, ClientsModel}
import slick.jdbc
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery
import utils.ClientsTable
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object BankManager {
  def props(db: PostgresProfile.backend.Database): Props = Props(new BankManager(db))

//  case class Response(statusCode: Int, message: String)
  case class StatusInfo(statusCode: Int, message: String)
  case class Response(int: Int)

  /**
    * Account messages
    */

//  case class CreateAccount(clientId: Int, typeOf: String)
//  case object GetAccounts
//  case class GetAccount(id: Int)
//  case class UpdatedAccount(id: Int, typeOf: String)
//  case class CloseAccount(id: Int)
//  case class Accounts(accounts: Seq[AccountModel])

//  case class ReplenishAnAccount(accountId: Int, value: Int)
//  case class WithdrawFromAccount(accountId: Int, value: Int)

//  case class BalanceTransfer(fAccountId: Int, tAccountId: Int, value: Int, initSender: ActorRef)

  /**
    * Client messages
    */

  case class CreateClient(name: String, age: Int, idCardNumber: String, phone: String, address: String)
  case object GetClients
  case class GetClient(id: Int)
  case class UpdateClient(clientId: Int, name: String, age: Int, idCardNumber: String, phone: String, address: String)
  case class DeleteClient(id: Int)
//  case class Clients(clients: Seq[ClientModel])
}

class BankManager(db: jdbc.PostgresProfile.backend.Database) extends Actor with ActorLogging {
  import BankManager._

  val clientsTable: TableQuery[ClientsTable] = TableQuery[ClientsTable]
  var clientsQuery: Seq[ClientModel] = Await.result(db.run(clientsTable.result), 5 seconds)

  override def receive: Receive = {
    case CreateClient(clientName, clientAge, clientIdCardNumber, clientPhone, clientAddress) =>
      sender() ! db.run(
        clientsTable += ClientModel(name = clientName, age = clientAge,
          idCardNumber =  clientIdCardNumber, phone = clientPhone, address = clientAddress)
      )

    case GetClients =>
      log.info("acdsasdcasdcasdcasdcasdcasdc")
      val a = Await.result(db.run(clientsTable.result), 5 seconds)
      sender() ! ClientsModel(a)

    case GetClient(clientId) =>
      sender() ! db.run(clientsTable.filter(_.id === clientId).result.head)

    case client: UpdateClient =>
      db.run(clientsTable.filter(_.id === client.clientId).map(cl => (cl.name, cl.age, cl.idCardNumber, cl.phone, cl.address)).update(client.name, client.age, client.idCardNumber, client.phone, client.address))
      sender() ! db.run(clientsTable.filter(_.id === client.clientId).result.head)

    case DeleteClient(clientId) =>
      sender() ! db.run(clientsTable.filter(_.id === clientId).delete)
  }
}
