package com.ll.infra

object Ws {
  //incoming messages from client
  sealed trait In
  case class Ping(id: Int) extends In

  //outgoing messages to client
  sealed trait Out
  case class Pong(id: Int) extends Out
  case class Text(txt: String) extends Out

}
