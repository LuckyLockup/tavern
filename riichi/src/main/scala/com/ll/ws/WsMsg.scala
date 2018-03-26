package com.ll.ws


object WsMsg {
  sealed trait In

  object In {
    case class Ping(id: Int) extends In
  }

  sealed trait Out
  object Out {
    case class Pong(id: Int) extends Out
    case class Text(txt: String) extends Out
  }
}
