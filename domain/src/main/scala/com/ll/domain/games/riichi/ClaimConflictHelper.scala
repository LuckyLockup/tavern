package com.ll.domain.games.riichi

import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.persistence.TableCmd.RiichiCmd
import com.ll.domain.persistence.{RiichiEvent, TableEvent}

object ClaimConflictHelper {
  type Actions = Map[PlayerPosition[Riichi], List[RiichiCmd]]

  def resolveRon(
    possibleCmds: List[RiichiCmd],
    pendingEvents: PendingEvents,
    config: RiichiConfig,
    ron: RiichiEvent.RonDeclared,
  ): List[TableEvent[Riichi]] = {
    val rons = possibleCmds
      .filter(cmd => cmd.position != ron.position)
      .collect { case cmd: RiichiCmd.DeclareRon => cmd }
    if (rons.nonEmpty) {
      List(RiichiEvent.PendingEvent(ron))
    } else {
      resolveEvents(pendingEvents.copy(rons = ron :: pendingEvents.rons), config)
    }
  }

  def resolvePung(
    possibleCmds: List[RiichiCmd],
    pendingEvents: PendingEvents,
    config: RiichiConfig,
    pung: RiichiEvent.PungClaimed,
  ): List[TableEvent[Riichi]] = {
    val newCmds = possibleCmds.filter(cmd => cmd.position != pung.position)
    val rons = newCmds.collect { case cmd: RiichiCmd.DeclareRon => cmd }
    val newPendingEvent = pendingEvents.copy(pung = Some(pung))
    if (rons.nonEmpty) {
      List(RiichiEvent.PendingEvent(pung))
    } else {
      resolveEvents(newPendingEvent, config)
    }
  }

  def resolveChow(
    possibleCmds: List[RiichiCmd],
    pendingEvents: PendingEvents,
    config: RiichiConfig,
    chow: RiichiEvent.ChowClaimed,
  ): List[TableEvent[Riichi]]  = {
    val newCmds = possibleCmds.filter(cmd => cmd.position != chow.position)
    val rons = newCmds.collect { case cmd: RiichiCmd.DeclareRon => cmd }
    val pungs = newCmds.collect { case cmd: RiichiCmd.ClaimPung => cmd }
    val newPendingEvents = pendingEvents.copy(chow = Some(
      RiichiEvent.ChowClaimed(chow.tableId, chow.gameId, chow.turn, chow.position, chow.from, chow.claimedTile, chow.tiles)))
    if (rons.nonEmpty || pungs.nonEmpty) {
      List(RiichiEvent.PendingEvent(chow))
    } else {
      resolveEvents(newPendingEvents, config)
    }
  }

  def resolveEvents(events: PendingEvents, config: RiichiConfig): List[TableEvent[Riichi]] = events match {
    case PendingEvents(nextTile, Nil, None, None)    => List(nextTile)
    case PendingEvents(_, Nil, None, Some(chow))     => List(chow)
    case PendingEvents(_, Nil, Some(pung), _)        => List(pung)
    case PendingEvents(_, ron :: Nil, _, _)          => List(RiichiEvent.RonDeclared(
      ron.tableId,
      ron.gameId,
      ron.turn,
      ron.position,
      ron.from
    ))
    case PendingEvents(_, ron1 :: ron2 :: Nil, _, _) => List(RiichiEvent.DoubleRonDeclared(
      ron1.tableId,
      ron1.gameId,
      ron1.turn,
      ron1.from,
      (ron1.position, ron2.position)
    ))
    case PendingEvents(nextTile: RiichiEvent.TileFromTheWallTaken, rons, _, _)         => List(RiichiEvent.DrawDeclared(
      nextTile.tableId,
      nextTile.gameId,
      nextTile.turn)
    )
    case PendingEvents(nextTile: RiichiEvent.DrawDeclared, rons, _, _)         => List(RiichiEvent.DrawDeclared(
      nextTile.tableId,
      nextTile.gameId,
      nextTile.turn)
    )
  }
}
