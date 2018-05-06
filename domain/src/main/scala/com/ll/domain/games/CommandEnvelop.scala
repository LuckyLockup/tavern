package com.ll.domain.games

import com.ll.domain.auth.User
import com.ll.domain.ws.WsMsgIn.WsTableCmd

case class CommandEnvelop(
  cmd: WsTableCmd,
  sender: User
) {
  def tableId = cmd.tableId
}