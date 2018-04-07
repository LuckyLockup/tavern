package com.ll.domain.json

import com.ll.domain.auth.UserId
import com.ll.domain.games.{GameId, TableId}
import com.ll.domain.messages.WsMsg.Out.Pong
import com.ll.domain.persistence.{TableCmd, UserCmd}
import org.scalatest.{FunSuite, Matchers}

class CodecTest extends FunSuite with Matchers{
  import Codec._

  test("testEncodeWsMsg") {
    encodeWsMsg(Pong(42)) shouldBe ("""{"Pong":{"id":42}}""")
//    encodeWsMsg(UserCmd.JoinAsPlayer(TableId(42), GameId(140))) shouldBe ("""{"PlayerJoinedTheGame":{"userId":42}}""")
  }

  test("testDecodeWsMsg") {

  }

  /**
    *   test("testEncodeWs") {
    import com.ll.domain.messages.WsMsg.Out._
    encodeWsMsg(Pong(42)) shouldBe ("""{"Pong":{"id":42}}""")
    encodeWsMsg(PlayerJoinedTheGame(UserId(42))) shouldBe ("""{"PlayerJoinedTheGame":{"userId":42}}""")
  }

  test("testDecodeWs") {
    import com.ll.domain.messages.WsMsg.In._
    Ping(42)
    decodeWsMsg("""{"Ping":{"id":42}}""") shouldBe Right(Ping(42))
    decodeWsMsg("""{"Ping":{"id":42,"crap":43}}""") shouldBe Right(Ping(42))
    decodeWsMsg("""{"JoinGameAsPlayer":{"gameId":42}}""") shouldBe Right(JoinGameAsPlayer(GameId(42)))
    decodeWsMsg("""{"GetState":{"gameId":42}}""") shouldBe Right(GetState(GameId(42)))
    //{"DiscardTile":{"gameId":42, "tile": "5_wan"}}
  }
    */
}
