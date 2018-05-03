package com.ll.domain.games

import com.ll.domain.ws.WsMsgIn.{CommonCmd, GameCmd}

import scala.concurrent.duration.FiniteDuration

case class ScheduledCommand[GT<: GameType](
  duration: FiniteDuration,
  cmd: GameCmd[GT]
)
