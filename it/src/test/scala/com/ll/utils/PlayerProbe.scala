package com.ll.utils

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.Player.HumanPlayer
import com.ll.domain.games.{GameId, TableId}
import org.scalatest.Matchers
import com.ll.domain.messages.HttpMessage.Riichi.CreateTable
import com.ll.domain.ws.{WsMsgIn, WsMsgOut}
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd

import scala.concurrent.Await
import io.circe.syntax._

import scala.util.Random

case class PlayerProbe(userId: UserId, ws: WsConnection, http: HttpExt, config: TestConfig)
(implicit as: ActorSystem, mat: Materializer) extends Logging with Matchers  {
  implicit val ec = as.dispatcher

  val name = Random.alphanumeric.take(6).mkString("")
  val user = User(userId, name)

  def !(msg: WsMsgIn): Unit = ws ! msg

  def createTable(id: TableId) = {
    val request = CreateTable(id, userId).asJson.noSpaces
    log.info(s"Http >> $request")
    var responseF = http.singleRequest(HttpRequest(
      method = HttpMethods.POST,
      uri = config.soloUrl,
      entity =
        HttpEntity(ContentType(MediaTypes.`application/json`), request)
        ))
//      .flatMap(Unmarshal(_).to[Json])
    val response = Await.result(responseF, config.defaultTimeout)
    log.info(s"Http << $response")
    ws.expectWsMsgT[WsMsgOut.Riichi.RiichiState]()
  }

  def joinTable(tableId: TableId) = {
    ws ! WsRiichiCmd.JoinAsPlayer(tableId)
    ws.expectWsMsg {
      case joined@WsMsgOut.Riichi.PlayerJoinedTable(`tableId`, HumanPlayer(User(`userId`, _), _)) =>
        joined
    }
  }

  def skipAction(tableId: TableId, gameId: GameId, turn: Int): Unit = {
    ws !  WsRiichiCmd.SkipAction(tableId, gameId, turn)
    ws.expectWsMsg{
      case msg@WsMsgOut.Riichi.ActionSkipped(`tableId`, `gameId`, `turn`) => msg
    }
  }
}
