package com.ll.domain.games

import com.ll.domain.ai.AIType
import com.ll.domain.auth.User
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.json.CaseClassCodec
import io.circe.{Decoder, Encoder}
import io.circe.syntax._

sealed trait Player[GT <: GameType] {
  def nickName: String
  def position: PlayerPosition[GT]
}

object Player extends CaseClassCodec {
  case class HumanPlayer[GT <: GameType](user: User, position: PlayerPosition[GT]) extends Player[GT] {
    def nickName = user.nickname
  }

  case class AIPlayer[GT <: GameType](ai: AIType[GT], position: PlayerPosition[GT]) extends Player[GT] {
    def nickName = ai.getClass.getName
  }

  implicit lazy val HumanPlayerEncoder: Encoder[HumanPlayer[Riichi]] = encoder[HumanPlayer[Riichi]]("HumanPlayer")
  implicit lazy val HumanPlayerDecoder: Decoder[HumanPlayer[Riichi]] = decoder[HumanPlayer[Riichi]]("HumanPlayer")

  implicit lazy val AIPlayerEncoder: Encoder[AIPlayer[Riichi]] = encoder[AIPlayer[Riichi]]("AIPlayer")
  implicit lazy val AIPlayerDecoder: Decoder[AIPlayer[Riichi]] = decoder[AIPlayer[Riichi]]("AIPlayer")

  implicit lazy val PlayerEncoder: Encoder[Player[Riichi]] = Encoder.instance {
    case p: HumanPlayer[Riichi] => p.asJson
    case p: AIPlayer[Riichi]    => p.asJson
  }

  implicit lazy val PlayerDecoder: Decoder[Player[Riichi]] = Decoder.instance { cur =>
    import cats.syntax.either._
    HumanPlayerDecoder.apply(cur) orElse
      AIPlayerDecoder.apply(cur)
  }
}
