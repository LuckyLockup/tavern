package com.ll.utils

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.{GameId, TableId}
import com.ll.domain.messages.WsMsg
import io.circe.Json
import org.scalatest.Matchers
import com.ll.domain.messages.HttpMessage.Riichi
import com.ll.domain.messages.HttpMessage.Riichi.CreateTable

import scala.concurrent.Await
import io.circe.syntax._

import scala.util.Random

case class PlayerProbe(userId: UserId, ws: WsConnection, http: HttpExt, config: TestConfig)
(implicit as: ActorSystem, mat: Materializer) extends Logging with Matchers  {
  import com.ll.domain.json.Codec._
  implicit val ec = as.dispatcher

  val name = Random.alphanumeric.take(6).mkString("")
  val user = User(userId, name)

  def !(msg: WsMsg.In): Unit = ws ! msg

  def createTable(id: TableId) = {
    var responseF = http.singleRequest(HttpRequest(
      method = HttpMethods.POST,

      uri = config.soloUrl,
      entity =
        HttpEntity(ContentType(MediaTypes.`application/json`), CreateTable(id, userId).asJson.noSpaces)
        ))
//      .flatMap(Unmarshal(_).to[Json])
    val response = Await.result(responseF, config.defaultTimeout)
    log.info(s"Http << ${response}")
  }

}
