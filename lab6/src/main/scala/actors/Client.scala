package actors

import akka.actor.{Actor, ActorLogging, Props}
import models.ClientModelGet

object Client {
  def props(id: BigInt): Props = Props(new Client(id))

  case object GetData
  case class SetData(firstName: String, lastName: String, birthday: String)
}

class Client(id: BigInt) extends Actor with ActorLogging{
  import Client._

  override def preStart(): Unit = log.info(s"Client $id started")
  override def postStop(): Unit = log.info(s"Client $id stopped")

  var state: ClientModelGet = ClientModelGet(id, "", "", "")

  override def receive: Receive = {
    case GetData =>
      log.info("Received GetData request")
      sender() ! state

    case SetData(clientFirstName, clientLastName, birthday) =>
      log.info("Received SetData request")
      state = ClientModelGet(state.id, clientFirstName, clientLastName, birthday)
      sender() ! state
  }

}
