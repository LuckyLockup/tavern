package com.ll.domain.auth

case class UserId(id: Long) extends AnyVal {
  override def toString: String = id.toString
}