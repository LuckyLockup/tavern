package com.ll.domain.json

import com.ll.domain.auth.UserId
import com.ll.domain.games.{GameId, TableId, User}
import com.ll.domain.json.Codec.{Test, encodeWsMsg}
import com.ll.domain.messages.WsMsg
import org.scalatest.{Matchers, WordSpec}
import gnieh.diffson.circe._

class CodecTest extends WordSpec with Matchers {

  "Encode Out messages" in new OutMessages {
    testData.foreach { case (msg, json) =>
      JsonDiff.diff(encodeWsMsg(msg), json, false).ops shouldBe empty
    }
  }

  "Decode Out messages" in new OutMessages {
    testData.foreach { case (msg, json) =>
      Test.decodeWsMsg(json) should be (Right(msg))
    }
  }

  trait Common {
    val userId = UserId(42)
    val user = User("Akagi", userId)
    val tableId = TableId("test_table")
    val gameId = GameId(100)
  }

  abstract class OutMessages extends Common {
    import com.ll.domain.messages.WsMsg.Out._
    import com.ll.domain.messages.WsMsg.Out.Table._
    val pong = Pong(42)
    val text = Text("hey!")
    val spectacularJoined = SpectacularJoinedTable(user, tableId)
    val spectacularLeft = SpectacularLeftTable(user, tableId)

    val testData: List[(WsMsg.Out, String)] = List(
      (Pong(42), """{"type":"Pong","payload":{"id":42}}"""),
      (Text("hey!"), """{"type":"Text","payload":{"txt":"hey!"}}"""),
      (SpectacularJoinedTable(user, tableId),
        """
          |{
          |  "type": "SpectacularJoinedTable",
          |  "payload": {
          |    "user": {
          |      "nickName": "Akagi",
          |      "userId": 42
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
          |      "nickName": "Akagi",
          |      "userId": 42
          |    },
          |    "tableId": "test_table"
          |  }
          |}
        """.stripMargin)
    )
  }
}
