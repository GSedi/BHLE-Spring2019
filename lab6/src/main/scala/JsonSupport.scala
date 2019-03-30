import actors.BankManager
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import models.{AccountModelPostPut, AccountModelGet, ClientModelGet, ClientModelPostPut, ReplenishAnAccountModel, WithdrawFromAccountModel}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat


trait JsonSupport {
//  implicit val bankManagerResponseFormat: RootJsonFormat[BankManager.Response] = jsonFormat2(BankManager.Response)
  implicit val bankManagerStatusInfoFormat: RootJsonFormat[BankManager.StatusInfo] = jsonFormat2(BankManager.StatusInfo)
//  implicit val bankManagerResponseFormat: RootJsonFormat[BankManager.Response[AccountModel]] = jsonFormat4(BankManager.Response[AccountModel])
//  implicit val bankManagerResponseFormatClient: RootJsonFormat[BankManager.Response[ClientModel]] = jsonFormat4(BankManager.Response[ClientModel])
  implicit val accountModelGetFormat: RootJsonFormat[AccountModelGet] = jsonFormat4(AccountModelGet)
  implicit val accountModelPostPutFormat: RootJsonFormat[AccountModelPostPut] = jsonFormat2(AccountModelPostPut)
  implicit val bankManagerAccountsFormat: RootJsonFormat[BankManager.Accounts] = jsonFormat1(BankManager.Accounts)
  implicit val clientModelGetFormat: RootJsonFormat[ClientModelGet] = jsonFormat2(ClientModelGet)
  implicit val clientModelPostPutFormat: RootJsonFormat[ClientModelPostPut] = jsonFormat1(ClientModelPostPut)
  implicit val bankManagerClientsFormat: RootJsonFormat[BankManager.Clients] = jsonFormat1(BankManager.Clients)
  implicit val replenishAnAccountModel: RootJsonFormat[ReplenishAnAccountModel] = jsonFormat1(ReplenishAnAccountModel)
  implicit val withdrawFromAccountModel: RootJsonFormat[WithdrawFromAccountModel] = jsonFormat1(WithdrawFromAccountModel)
  //  implicit val responseObjectFormat: RootJsonFormat[BankManager.ResponseObject] = jsonFormat1(BankManager.ResponseObject)
//  implicit val objectFormat: RootJsonFormat[] = jsonFormat1()
}