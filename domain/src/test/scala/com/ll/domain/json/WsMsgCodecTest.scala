package com.ll.domain.json

import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.Player.HumanPlayer
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.riichi.RiichiConfig
import com.ll.domain.games.riichi.result.TablePoints
import com.ll.domain.games.{GameId, TableId}
import com.ll.domain.ws.WsMsgCodec.{Test, decodeWsMsg, encodeWsMsg}
import com.ll.domain.ws.{WsMsgIn, WsMsgOut}
import com.ll.domain.ws.WsMsgIn.Ping
import com.ll.domain.ws.WsMsgIn.RiichiWsCmd.StartGame
import com.ll.domain.ws.WsMsgOut.Riichi._
import com.ll.domain.ws.WsMsgOut.{Message, Pong, SpectacularJoinedTable, SpectacularLeftTable}
import org.scalatest.{Matchers, WordSpec}
import gnieh.diffson.circe._

class WsMsgCodecTest extends WordSpec with Matchers {

  "Encode Out messages" in new OutMessages {
    testData.foreach { case (msg, json) =>
      println(encodeWsMsg(msg))
      JsonDiff.diff(encodeWsMsg(msg), json, false).ops shouldBe empty
    }
  }

  "Decode Out messages" in new OutMessages {
    testData.foreach { case (msg, json) =>
      Test.decodeWsOutMsg(json) should be (Right(msg))
    }
  }

  "Encode In messages" in new InMessages {
    testData.foreach { case (msg, json) =>
      println(Test.encodeWsInMsg(msg))
      JsonDiff.diff(Test.encodeWsInMsg(msg), json, false).ops shouldBe empty
    }
  }

  "Decode In messages" in new InMessages {
    testData.foreach { case (msg, json) =>
      decodeWsMsg(json) should be (Right(msg))
    }
  }

  trait Common {
    val userId = UserId(42)
    val user = User(userId, "Akagi")
    val tableId = TableId("test_table")
    val gameId = GameId(100)
  }

  abstract class OutMessages extends Common {

    val testData: List[(WsMsgOut, String)] = List(
      (Pong(42), """{"type":"Pong","payload":{"id":42}}"""),
      (Message("hey!"), """{"type":"Message","payload":{"txt":"hey!"}}"""),
      (SpectacularJoinedTable(user, tableId),
        """
          |{
          |  "type": "SpectacularJoinedTable",
          |  "payload": {
          |    "user": {
          |      "id": 42,
          |      "nickname": "Akagi"
          |    },
          |    "tableId": "test_table"
          |  }
          |}
        """.stripMargin),

      (SpectacularLeftTable(user, tableId),
        """
          |{
          |  "type": "SpectacularLeftTable",
          |  "payload": {
          |    "user": {
          |      "id": 42,
          |      "nickname": "Akagi"
          |    },
          |    "tableId": "test_table"
          |  }
          |}
        """.stripMargin),
      (RiichiState(
        tableId = TableId("table_22"),
        admin = user,
        states = Nil,
        uraDoras = Nil,
        deck = 0,
        turn = 0,
        points = TablePoints.initialPoints
      ),
        """
          |{
          |  "type": "RiichiState",
          |  "payload": {
          |    "tableId": "table_22",
          |    "players": []
          |  }
          |}
        """.stripMargin),
      (PlayerJoinedTable(tableId, HumanPlayer(user, RiichiPosition.EastPosition)),
        """
          |{
          |  "type": "PlayerJoinedTable",
          |  "payload": {
          |    "tableId": "test_table",
          |    "user": {
          |      "type": "HumanPlayer",
          |      "payload": {
          |        "user": {
          |          "id": 42,
          |          "nickname": "Akagi"
          |        },
          |        "position": "EastPosition"
          |      }
          |    }
          |  }
          |}
        """.stripMargin)
    )
  }

  abstract class InMessages extends Common {


    val testData: List[(WsMsgIn, String)] = List(
      (Ping(42), """{"type":"Ping","payload":{"id":42}}"""),
      (StartGame(tableId, gameId, RiichiConfig()),
        """
          |{
          |  "type": "StartGame",
          |  "payload": {
          |    "tableId": "test_table",
          |    "gameId": 100
          |  }
          |}
        """.stripMargin)

    )
  }
}
