package com.ll.utils

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.ll.domain.auth.UserId
import com.ll.domain.games.GameId
import com.ll.domain.messages.WsMsg
import io.circe.Json
import org.scalatest.Matchers

import scala.concurrent.Await
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

case class Player(userId: UserId, ws: WsConnection, http: HttpExt, config: TestConfig)
(implicit as: ActorSystem, mat: Materializer) extends Logging with Matchers  {
  implicit val ec = as.dispatcher

  def !(msg: WsMsg.In): Unit = ws ! msg

  def createGame(id: GameId) = {
    var responseF = http.singleRequest(HttpRequest(
      method = HttpMethods.POST,
      uri = config.soloUrl,
      entity = id.id.toString))
      .flatMap(Unmarshal(_).to[Json])
    val response = Await.result(responseF, config.defaultTimeout)
    log.info(s"Http << ${response}")
  }
}
