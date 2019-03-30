package models

import utils.AccountType

case class AccountModelGet(id: BigInt, clientId: BigInt, typeOf: String, volume: BigInt) extends Model

case class AccountModelPostPut(clientId: BigInt, typeOf: String)
