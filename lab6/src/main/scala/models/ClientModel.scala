package models

import akka.http.scaladsl.model.DateTime

case class ClientModelGet(id: BigInt, firstName: String, lastName: String, birthday: String) extends Model

case class ClientModelPostPut(firstName: String, lastName: String, birthday: String)
