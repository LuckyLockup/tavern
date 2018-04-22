package com.ll.domain.messages

import com.ll.domain.auth.UserId
import com.ll.domain.games.TableId
import com.ll.domain.json.CaseClassCodec
import io.circe.{Decoder, Encoder}

sealed trait HttpMessage

object HttpMessage {
  object Riichi {
    case class CreateTable(tableId: TableId, userId: UserId) extends HttpMessage

    case object CreateTable extends CaseClassCodec {
      implicit lazy val createTableEncoder: Encoder[HttpMessage.Riichi.CreateTable] =
        encoder[HttpMessage.Riichi.CreateTable]("CreateTable")
      implicit lazy val createTableDecoder: Decoder[HttpMessage.Riichi.CreateTable] =
        decoder[HttpMessage.Riichi.CreateTable]("CreateTable")
    }
  }
}
