package com.ll.domain.auth

import com.ll.domain.json.CommonTypesCodec

case class UserId(id: Long) extends AnyVal {
  override def toString: String = id.toString
}

object UserId extends CommonTypesCodec