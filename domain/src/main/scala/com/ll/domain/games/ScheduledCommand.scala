package com.ll.domain.games

import com.ll.domain.ws.WsMsgIn.{JoinLeftCmd, PlayerCmd}

import scala.concurrent.duration.FiniteDuration

case class ScheduledCommand[GT<: GameType](
  duration: FiniteDuration,
  cmd: PlayerCmd[GT]
)
