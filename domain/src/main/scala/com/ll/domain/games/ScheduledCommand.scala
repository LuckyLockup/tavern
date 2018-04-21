package com.ll.domain.games

import com.ll.domain.persistence.TableCmd

import scala.concurrent.duration.FiniteDuration

case class ScheduledCommand(
  duration: FiniteDuration,
  cmd: TableCmd
)
