import actors.BankManager
import actors.BankManager.Response
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import models.{AccountModel, ClientModel, ClientsModel, FullName}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat


trait JsonSupport {
//implicit val fullNameGetFormat: RootJsonFormat[FullName] = jsonFormat2(FullName)
  implicit val clientModelFormat: RootJsonFormat[ClientModel] = jsonFormat6(ClientModel)
  implicit val clientsModelFormat: RootJsonFormat[ClientsModel] = jsonFormat1(ClientsModel)
  implicit val accountModelFormat: RootJsonFormat[AccountModel] = jsonFormat4(AccountModel)
  implicit val responseFormat: RootJsonFormat[Response] = jsonFormat1(Response)
}