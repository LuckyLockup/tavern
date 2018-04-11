package com.ll.domain.json

import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.{GameId, TableId}
import com.ll.domain.json.Codec.{Test, decodeWsMsg, encodeWsMsg}
import com.ll.domain.messages.WsMsg
import org.scalatest.{Matchers, WordSpec}
import gnieh.diffson.circe._

class CodecTest extends WordSpec with Matchers {

  "Encode Out messages" in new OutMessages {
    testData.foreach { case (msg, json) =>
      println(encodeWsMsg(msg))
      JsonDiff.diff(encodeWsMsg(msg), json, false).ops shouldBe empty
    }
  }

  "Decode Out messages" in new OutMessages {
    testData.foreach { case (msg, json) =>
      Test.decodeWsMsg(json) should be (Right(msg))
    }
  }

  "Encode In messages" in new InMessages {
    testData.foreach { case (msg, json) =>
      println(Test.encodeWsMsg(msg))
      JsonDiff.diff(Test.encodeWsMsg(msg), json, false).ops shouldBe empty
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
    import com.ll.domain.messages.WsMsg.Out._
    import com.ll.domain.messages.WsMsg.Out.Table._

    val testData: List[(WsMsg.Out, String)] = List(
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
      (TableState(TableId("table_22"), Set.empty),
        """
          |{
          |  "type": "TableState",
          |  "payload": {
          |    "tableId": "table_22",
          |    "players": []
          |  }
          |}
        """.stripMargin)
    )
  }

  abstract class InMessages extends Common {
    import com.ll.domain.messages.WsMsg.In._
    import com.ll.domain.persistence.TableCmd._
    import com.ll.domain.persistence.UserCmd._
    import com.ll.domain.persistence.RiichiCmd._


    val testData: List[(WsMsg.In, String)] = List(
      (Ping(42), """{"type":"Ping","payload":{"id":42}}"""),
      (StartGame(tableId),
        """
          |{
          |  "type": "TableCmd.StartGame",
          |  "payload": {
          |    "tableId": "test_table"
          |  }
          |}
        """.stripMargin)

    )
  }
}
