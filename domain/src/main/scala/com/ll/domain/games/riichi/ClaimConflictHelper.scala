package com.ll.domain.games.riichi

import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.persistence.TableCmd.RiichiCmd
import com.ll.domain.persistence.TableCmd.RiichiCmd.{ClaimChow, ClaimPung, DeclareRon}
import com.ll.domain.persistence.{RiichiEvent, TableCmd, TableEvent}


object ClaimConflictHelper {
  type Actions = Map[PlayerPosition[Riichi], List[RiichiCmd]]

  def resolveRon(
    possibleActions: Actions,
    pendingCmds: List[RiichiCmd],
    cmd: DeclareRon,
    config: RiichiConfig
  ): Either[(Actions, List[TableCmd[Riichi]]), TableEvent[Riichi]] = {
    val possibleActionsForOtherPlayers: List[RiichiCmd] = possibleActions.flatMap {
      case (pos, _) if pos == cmd.position => Nil
      case (_, cmds)                       => cmds
    }.toList
    val rons = possibleActionsForOtherPlayers.collect { case c: DeclareRon => c }.size
    if (rons != 0) {
      Left((possibleActions - cmd.position, cmd :: pendingCmds))
    } else {
      //TODO double Ron and triple ron.
      Right(resolveCommands(cmd :: pendingCmds))
    }
  }

  def resolvePung(
    possibleActions: Actions,
    pendingCmds: List[RiichiCmd],
    cmd: ClaimPung,
    config: RiichiConfig
  ): Either[(Actions, List[TableCmd[Riichi]]), RiichiCmd] = {
   ???
  }

  def resolveCommands(pendingCmds: List[RiichiCmd]): TableEvent[Riichi] = {
    lazy val rons = pendingCmds.collect { case c: DeclareRon => c }
    lazy val chows = pendingCmds.collect { case c: ClaimChow => c }
    lazy val pungs = pendingCmds.collect { case c: ClaimPung => c }
    lazy val scheduledCommand = pendingCmds.last

    (rons, pungs, chows) match {
      case (Nil, Nil, Nil) => ??? //TODO scheduled command
      case (Nil, Nil, chow :: tail) => ??? //TODO declare chow
      case (Nil, pung :: tail, _) => ??? //TOOD declare pung
      case (ron :: Nil, _, _) => RiichiEvent.RonDeclared(ron.tableId, ron.gameId, ron.turn, ron.from, ron.position)
      case (ron1 :: ron2 :: Nil, _, _)=> ??? // TODO declare double ron
      case (ron1 :: ron2 :: ron3 :: Nil, _, _) => ??? // TODO declare draw
    }
  }
}
