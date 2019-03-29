package actors

import akka.actor.{Actor, ActorLogging, Props}
import models.ClientModel

object Client {
  def props(id: Long, name: String): Props = Props(new Client(id, name))

  case object GetData
  case class SetData(name: String)
}

class Client(id: Long, name: String) extends Actor with ActorLogging{
  import Client._

  override def preStart(): Unit = log.info(s"Client $name started")
  override def postStop(): Unit = log.info(s"Client $name stopped")

  override def receive: Receive = {
    case GetData =>
      log.info("Received GetData request")
      sender() ! ClientModel(id, name)

    case SetData(clientName) =>
      log.info("Received SetData request")
      sender() ! ClientModel(id, clientName)
  }

}
