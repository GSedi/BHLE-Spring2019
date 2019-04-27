package models

case class FullName(firstName: String, lastName: String)

case class ClientModel(
                        id: Option[Int] = None,
                        name: String,
                        age: Int,
                        idCardNumber: String,
                        phone: String,
                        address: String
                      )

case class ClientsModel(clients: Seq[ClientModel])
