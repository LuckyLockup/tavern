package com.ll.domain.games

import com.ll.domain.ai.ServiceId
import com.ll.domain.auth.{User}
import com.ll.domain.ws.WsMsgIn.CommonCmd

case class CommandEnvelop(
  cmd: CommonCmd,
  user: Either[ServiceId, User]
) {
  def tableId = cmd.tableId
  def sender = user.map(_.id)
}