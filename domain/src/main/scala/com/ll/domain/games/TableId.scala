package com.ll.domain.games

import io.circe._

case class TableId(id: String) {
  override def toString: String = id
}

object TableId  {
  implicit val TableIdEncode: Encoder[TableId] = (table: TableId) => Json.fromString(table.id)
  implicit val TableIdDecoder: Decoder[TableId] = (c: HCursor) => {
    c.focus.flatMap(_.asString) match {
      case None     => Left(DecodingFailure("not a string", Nil))
      case Some(id) => Right(TableId(id))
    }
  }
}
