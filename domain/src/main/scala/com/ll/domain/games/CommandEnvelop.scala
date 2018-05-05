package com.ll.domain.games

import com.ll.domain.ai.ServiceId
import com.ll.domain.auth.User
import com.ll.domain.ws.WsMsgIn.WsTableCmd

case class CommandEnvelop(
  cmd: WsTableCmd,
  sender: Either[ServiceId, User]
) {
  def tableId = cmd.tableId
}