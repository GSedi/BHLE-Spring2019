import actors.BankManager
import actors.BankManager.Response
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives.path
import models.{AccountModel, ClientModel, ClientsModel}

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.japi.Option.Some
import akka.pattern.ask
import akka.util.Timeout
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory
import utils.{AccountsTable, ClientsTable}
import slick.jdbc
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.duration._

object Boot extends App with JsonSupport {
  val log = LoggerFactory.getLogger("Boot")

  // needed to run the route
  implicit val system: ActorSystem = ActorSystem()

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit val timeout: Timeout = Timeout(30 seconds)

  val db: PostgresProfile.backend.Database = Database.forConfig("postgres")

//  val clients  = TableQuery[ClientsTable]
//  val accounts = TableQuery[AccountsTable]


  val bankManager: ActorRef = system.actorOf(BankManager.props(db))

  val route =
    pathPrefix("bank") {
      path("healthcheck") {
        pathEndOrSingleSlash {
          get {
            complete {
              log.info("Received healthcheck, replying with OK")
              "OK"
            }
          }
        }
      }~
      path("clients") {
        get {
          complete {
            (bankManager ? BankManager.GetClients)
              .mapTo[ClientsModel]
//              .mapTo[Future[ClientsModel]].flatten.map(x => x)
          }
        }~
        post {
          entity(as[ClientModel]) { client =>
            complete {
              (bankManager ? BankManager.CreateClient(client.name, client.age, client.idCardNumber, client.phone, client.address))
//                .mapTo[Future[ClientModel]].flatten.map(x => ClientModel(x.id, x.name, x.age, x.idCardNumber, x.phone, x.address))
                .mapTo[Future[Int]].flatten.map(x => Response(x))
            }
          }
        }
      }~
      path("clients"/ IntNumber){ id =>
        get {
          complete{
            (bankManager ? BankManager.GetClient(id))
//              .mapTo[ClientModel]
              .mapTo[Future[ClientModel]].flatten.map(x => x)
          }
        }~
        put {
          entity(as[ClientModel]) {client =>
            complete {
              (bankManager ? BankManager.UpdateClient(id, client.name, client.age, client.idCardNumber, client.phone, client.address))
                .mapTo[Future[ClientModel]].flatten.map(x => x)
            }
          }
        }~
        delete {
          complete {
            (bankManager ? BankManager.DeleteClient(id))
              .mapTo[Future[Int]].flatten.map(x => Response(x))
          }
        }
      }
    }

//  val config = ConfigFactory.load()

//  val shouldCreate = false
//
//  if (shouldCreate) {
//    try {
//      Await.result(db.run(DBIO.seq(
//        clients.schema.create,
//        accounts.schema.create,
//
//        clients.result.map(println),
//        accounts.result.map(println)
//      )), Duration.Inf)
//    }finally db.close()
//  }


  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)
  log.info("Listening on port 8080...")
}
