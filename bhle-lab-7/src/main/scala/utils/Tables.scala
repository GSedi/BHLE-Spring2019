package utils
import models.{AccountModel, ClientModel, FullName}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape, Tag, TableQuery}

class ClientsTable(tag: Tag) extends Table[ClientModel](tag, "clients") {
  // This is the primary key column:
  def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name: Rep[String] = column[String]("name")
  def age: Rep[Int] = column[Int]("age")
  def idCardNumber: Rep[String] = column[String]("id_card_number")
  def phone: Rep[String] = column[String]("phone")
  def address: Rep[String] = column[String]("address")

  def * : ProvenShape[ClientModel] = (id.?, name, age, idCardNumber, phone, address) <> (ClientModel.tupled, ClientModel.unapply)
}

class AccountsTable(tag: Tag) extends Table[AccountModel](tag, "accounts") {
  def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def clientId: Rep[Int] = column[Int]("client_id")
  def publicId: Rep[String] = column[String]("public_id")
  def typeOf: Rep[String] = column[String]("type_of")

  def * : ProvenShape[AccountModel] = (id.?, clientId, publicId, typeOf) <> (AccountModel.tupled, AccountModel.unapply)
}