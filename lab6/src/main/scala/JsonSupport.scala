import actors.BankManager
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.DateTime
import models.{AccountModelGet, AccountModelPostPut, BalanceTransfer, ClientModelGet, ClientModelPostPut, ReplenishAnAccountModel, WithdrawFromAccountModel}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat


trait JsonSupport {
  implicit val bankManagerStatusInfoFormat: RootJsonFormat[BankManager.StatusInfo] = jsonFormat2(BankManager.StatusInfo)
  implicit val accountModelGetFormat: RootJsonFormat[AccountModelGet] = jsonFormat4(AccountModelGet)
  implicit val accountModelPostPutFormat: RootJsonFormat[AccountModelPostPut] = jsonFormat2(AccountModelPostPut)
  implicit val bankManagerAccountsFormat: RootJsonFormat[BankManager.Accounts] = jsonFormat1(BankManager.Accounts)
  implicit val clientModelGetFormat: RootJsonFormat[ClientModelGet] = jsonFormat4(ClientModelGet)
  implicit val clientModelPostPutFormat: RootJsonFormat[ClientModelPostPut] = jsonFormat3(ClientModelPostPut)
  implicit val bankManagerClientsFormat: RootJsonFormat[BankManager.Clients] = jsonFormat1(BankManager.Clients)
  implicit val replenishAnAccountModelFormat: RootJsonFormat[ReplenishAnAccountModel] = jsonFormat1(ReplenishAnAccountModel)
  implicit val withdrawFromAccountModelFormat: RootJsonFormat[WithdrawFromAccountModel] = jsonFormat1(WithdrawFromAccountModel)
  implicit val balanceTransferFormat: RootJsonFormat[BalanceTransfer] = jsonFormat2(BalanceTransfer)
}