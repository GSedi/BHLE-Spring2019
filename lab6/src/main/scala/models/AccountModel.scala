package models

import utils.AccountType

case class AccountModel(id: Long, clientId: Long, typeOf: String, volume: Double = 0.0)
