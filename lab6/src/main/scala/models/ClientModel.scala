package models

case class ClientModelGet(id: BigInt, name: String) extends Model

case class ClientModelPostPut(name: String)
