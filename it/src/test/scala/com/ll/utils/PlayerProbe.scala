package com.ll.utils

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.{GameId, HumanPlayer, TableId}
import com.ll.domain.messages.WsMsg
import io.circe.Json
import org.scalatest.Matchers

import scala.concurrent.Await
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

import scala.util.Random

case class PlayerProbe(userId: UserId, ws: WsConnection, http: HttpExt, config: TestConfig)
(implicit as: ActorSystem, mat: Materializer) extends Logging with Matchers  {
  implicit val ec = as.dispatcher

  val name = Random.alphanumeric.take(6).mkString("")
  val user = User(userId, name)
  val player = HumanPlayer(userId, name)

  def !(msg: WsMsg.In): Unit = ws ! msg

  def createTable(id: TableId) = {
    var responseF = http.singleRequest(HttpRequest(
      method = HttpMethods.POST,
      uri = config.soloUrl,
      entity = id.id.toString))
//      .flatMap(Unmarshal(_).to[Json])
    val response = Await.result(responseF, config.defaultTimeout)
    log.info(s"Http << ${response}")
  }

}
