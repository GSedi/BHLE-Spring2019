
import actors.{Bank, BankManager}
import akka.actor.ActorRef
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives.path
import models.{AccountModelGet, AccountModelPostPut, BalanceTransfer, ClientModelGet, ClientModelPostPut, ReplenishAnAccountModel, WithdrawFromAccountModel}

import scala.concurrent.{Await, ExecutionContextExecutor}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import org.slf4j.LoggerFactory

import scala.concurrent.duration._


object BankApp extends App with JsonSupport {

  val log = LoggerFactory.getLogger("BankApp")

  implicit val timeout: Timeout = Timeout(30 seconds)

  // needed to run the route
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val bankBot: ActorRef = system.actorOf(BankBot.props(), "bank-bot")
  val result = bankBot ? BankBot.StartBank
  val bankManager = Await.result(result, timeout.duration).asInstanceOf[ActorRef]

  val route =
    pathPrefix("bank") {
      path("clients"){
        post {
          entity(as[ClientModelPostPut]) { clientModel =>
            complete {
              (bankManager ? BankManager.CreateClient(clientModel.firstName, clientModel.lastName, clientModel.birthday))
                .mapTo[Either[BankManager.StatusInfo, ClientModelGet]]
            }
          }
        } ~
        get {
          complete {
            (bankManager ? BankManager.GetClients)
              .mapTo[Either[BankManager.StatusInfo, BankManager.Clients]]
          }
        }
      }~
      path("clients" / IntNumber){ id =>
        get{
          complete {
            (bankManager ? BankManager.GetClient(id))
              .mapTo[Either[BankManager.StatusInfo, ClientModelGet]]
          }
        }~
        put{
          entity(as[ClientModelPostPut]) { clientModel =>
            complete {
              (bankManager ? BankManager.UpdateClient(id, clientModel.firstName, clientModel.lastName, clientModel.birthday))
                .mapTo[Either[BankManager.StatusInfo, ClientModelGet]]
            }
          }
        }~
        delete{
          complete {
            (bankManager ? BankManager.DeleteClient(id))
              .mapTo[Either[BankManager.StatusInfo, BankManager.StatusInfo]]
          }
        }
      }~
      path("accounts") {
        post {
          entity(as[AccountModelPostPut]) { accountModel =>
            complete {
              (bankManager ? BankManager.CreateAccount(accountModel.clientId, accountModel.typeOf))
                .mapTo[Either[BankManager.StatusInfo, AccountModelGet]]
            }
          }
        } ~
        get {
          complete {
            (bankManager ? BankManager.GetAccounts)
              .mapTo[Either[BankManager.StatusInfo, BankManager.Accounts]]
          }
        }
      }~
      pathPrefix("accounts" / IntNumber){ id =>
        get{
          complete {
            (bankManager ? BankManager.GetAccount(id))
              .mapTo[Either[BankManager.StatusInfo, AccountModelGet]]
          }
        }~
        put{
          entity(as[AccountModelPostPut]) { accountModel =>
            complete {
              (bankManager ? BankManager.UpdatedAccount(id, accountModel.typeOf) )
                .mapTo[Either[BankManager.StatusInfo, AccountModelGet]]
            }
          }
        }~
        delete {
          complete {
            (bankManager ? BankManager.CloseAccount(id))
              .mapTo[Either[BankManager.StatusInfo, BankManager.StatusInfo]]
          }
        }~
        pathPrefix("transaction"){
          path("replenish"){
            put{
              entity(as[ReplenishAnAccountModel]) { replenishAnAccountModel =>
                complete {
                  (bankManager ? BankManager.ReplenishAnAccount(id, replenishAnAccountModel.value))
                    .mapTo[Either[BankManager.StatusInfo, BankManager.StatusInfo]]
                }
              }
            }
          }~
          path("withdraw"){
            put{
              entity(as[WithdrawFromAccountModel]) { withdrawFromAccountModel =>
                complete {
                  (bankManager ? BankManager.WithdrawFromAccount(id, withdrawFromAccountModel.value))
                    .mapTo[Either[BankManager.StatusInfo, BankManager.StatusInfo]]
                }
              }
            }
          }~
          path("transfer"){
            put{
              entity(as[BalanceTransfer]) { balanceTransfer =>
                complete {
                  (bankManager ? BankManager.BalanceTransfer(id, balanceTransfer.tAccountId, balanceTransfer.value, bankManager))
                    .mapTo[Either[BankManager.StatusInfo, BankManager.StatusInfo]]
                }
              }
            }
          }
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  log.info("Listening on port 8080...")
}
