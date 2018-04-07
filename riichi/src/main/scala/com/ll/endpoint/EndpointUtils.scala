package com.ll.endpoint

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, onComplete}
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait EndpointUtils {

  def async[T](f: Future[T]): Route = onComplete(f){
    case Success(result) => complete(StatusCodes.Created, "OK")
    case Failure(ex) => complete(StatusCodes.InternalServerError, ex.getMessage)
  }
}
