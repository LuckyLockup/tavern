package com.ll.domain.ai

sealed trait AIType

object AIType {
  /**
    * This is basic AI variant which can only discard whatever title it gets from the wall.
    */
  case object Duck extends AIType
}