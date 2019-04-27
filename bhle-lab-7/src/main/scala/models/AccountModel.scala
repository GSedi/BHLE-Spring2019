package models

case class AccountModel(
                         id: Option[Int] = None,
                         clientId: Int,
                         publicId: String,
                         typeOf: String
                       )

case class AccountsModel(accounts: Seq[AccountModel])
