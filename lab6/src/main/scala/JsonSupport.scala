import actors.BankManager
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import models.{AccountModel, ClientModel}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat


trait JsonSupport {
  implicit val bankManagerResponseFormat: RootJsonFormat[BankManager.Response] = jsonFormat4(BankManager.Response)
  implicit val accountModelFormat: RootJsonFormat[AccountModel] = jsonFormat4(AccountModel)
  implicit val bankManagerAccountsFormat: RootJsonFormat[BankManager.Accounts] = jsonFormat1(BankManager.Accounts)
  implicit val clientModelFormat: RootJsonFormat[ClientModel] = jsonFormat2(ClientModel)
  implicit val bankManagerClientsFormat: RootJsonFormat[BankManager.Clients] = jsonFormat1(BankManager.Clients)
//  implicit val responseObjectFormat: RootJsonFormat[BankManager.ResponseObject] = jsonFormat1(BankManager.ResponseObject)
//  implicit val objectFormat: RootJsonFormat[] = jsonFormat1()
}
