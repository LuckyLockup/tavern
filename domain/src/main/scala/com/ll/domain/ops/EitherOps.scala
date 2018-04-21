package com.ll.domain.ops

import com.ll.domain.messages.WsMsg.Out.ValidationError

object EitherOps {
  //https://github.com/typelevel/mouse/blob/master/shared/src/main/scala/mouse/boolean.scala
  implicit final class BooleanOps(val expr: Boolean) extends AnyVal {
    def asEither(ifFalse: String): Either[ValidationError, Boolean] = {
      expr match {
        case true => Right(expr)
        case false => Left(ValidationError(ifFalse))
      }
    }
  }

  implicit class OptionOps[T](opt: Option[T]) {
    def asEither(ifNone: String): Either[ValidationError, T] = {
      opt match {
        case Some(v) => Right(v)
        case None => Left(ValidationError(ifNone))
      }
    }
  }
}
