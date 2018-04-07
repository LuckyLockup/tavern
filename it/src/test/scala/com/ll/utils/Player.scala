package com.ll.utils

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.ll.domain.auth.UserId
import com.ll.domain.games.GameId
import com.ll.domain.messages.WsMsg
import org.scalatest.Matchers

import scala.concurrent.Await


case class Player(userId: UserId, ws: WsConnection, http: HttpExt, config: TestConfig)
(implicit as: ActorSystem, mat: Materializer) extends Logging with Matchers {
  implicit val ec = as.dispatcher

  def !(msg: WsMsg.In): Unit = ws ! msg

  def createGame(id: GameId) = {
    var responseF = http.singleRequest(HttpRequest(
      method = HttpMethods.POST,
      uri = config.soloUrl,
      entity = id.id.toString))
    val response = Await.result(responseF, config.defaultTimeout)
    response.status should equal(StatusCodes.OK)
  }
}
