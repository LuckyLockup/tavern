package com.ll.ws

import com.ll.domain.auth.UserId
import com.ll.domain.games.GameId
import org.scalatest.FunSuite
import org.scalatest.Matchers._

class CodecTest extends FunSuite {
  import com.ll.domain.ws.Codec._

  test("testEncodeWs") {
    import com.ll.domain.ws.WsMsg.Out._
    encodeWsMsg(Pong(42)) shouldBe ("""{"Pong":{"id":42}}""")
    encodeWsMsg(PlayerJoinedTheGame(UserId(42))) shouldBe ("""{"PlayerJoinedTheGame":{"userId":42}}""")
  }

  test("testDecodeWs") {
    import com.ll.domain.ws.WsMsg.In._
    Ping(42)
    decodeWsMsg("""{"Ping":{"id":42}}""") shouldBe Right(Ping(42))
    decodeWsMsg("""{"Ping":{"id":42,"crap":43}}""") shouldBe Right(Ping(42))
    decodeWsMsg("""{"JoinGameAsPlayer":{"gameId":42}}""") shouldBe Right(JoinGameAsPlayer(GameId(42)))
    decodeWsMsg("""{"GetState":{"gameId":42}}""") shouldBe Right(GetState(GameId(42)))
    //{"DiscardTile":{"gameId":42, "tile": "5_wan"}}
  }
}
