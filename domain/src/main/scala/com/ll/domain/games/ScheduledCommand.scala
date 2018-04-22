package com.ll.domain.games

import com.ll.domain.ws.WsMsgIn.TableCmd

import scala.concurrent.duration.FiniteDuration

case class ScheduledCommand(
  duration: FiniteDuration,
  cmd: TableCmd
)
