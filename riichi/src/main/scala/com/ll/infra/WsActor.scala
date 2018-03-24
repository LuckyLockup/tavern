package com.ll.infra

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.ll.domain.auth.UserId

class WsActor extends Actor with ActorLogging{
  import WsActor._

  override def receive: Receive = withClients(Map.empty[UserId, ActorRef])

  def withClients(clients: Map[UserId, ActorRef]): Receive = {
    case SignedMessage(id, msg) =>
      log.info(s"Message received: $msg")
      clients.get(id).foreach(ar => ar ! msg)
    case OpenConnection(ar, uuid) =>
      log.info("Connection closed.")
      context.become(withClients(clients.updated(uuid, ar)))
    case CloseConnection(uuid) =>
      log.info("Connection closed.")
      context.become(withClients(clients - uuid))
  }
}

object WsActor {
  case class SignedMessage(uuid: UserId, msg: String)
  case class OpenConnection(actor: ActorRef, uuid: UserId)
  case class CloseConnection(uuid: UserId)

}