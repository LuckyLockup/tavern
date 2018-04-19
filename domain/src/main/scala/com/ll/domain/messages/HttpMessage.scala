package com.ll.domain.messages

import com.ll.domain.auth.UserId
import com.ll.domain.games.TableId

sealed trait HttpMessage

object HttpMessage {
  object Riichi {
    case class CreateTable(tableId: TableId, userId: UserId)
  }
}
