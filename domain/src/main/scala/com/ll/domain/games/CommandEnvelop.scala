package com.ll.domain.games

import com.ll.domain.ai.ServiceId
import com.ll.domain.auth.{User}
import com.ll.domain.ws.WsMsgIn.JoinLeftCmd

case class CommandEnvelop(
  cmd: JoinLeftCmd,
  user: Either[ServiceId, User]
) {
  def tableId = cmd.tableId
  def senderId = user.map(_.id)
}