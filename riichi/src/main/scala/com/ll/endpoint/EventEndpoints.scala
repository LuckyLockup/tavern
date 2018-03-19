package com.ll.endpoint


import cats.data.Validated.Valid
import cats.data._
import cats.effect.Effect
import cats.implicits._
import com.ll.domain.game.{Event, EventService}
import io.circe._
import io.circe.generic.auto._
import io.circe.generic.extras.semiauto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpService, QueryParamDecoder}

import scala.language.higherKinds


class EventEndpoints[F[_]: Effect] extends Http4sDsl[F] {

  import javafx.scene.control.Pagination._

  implicit val petDecoder: EntityDecoder[F, Event] = jsonOf[F, Event]

  private def createPetEndpoint(eventService: EventService[F]): HttpService[F] =
    HttpService[F] {
      case req @ POST -> Root / "events" =>
        val action = for {
          event <- req.as[Event]
          result <- eventService.save(event).value
        } yield result

        action.flatMap {
          case Right(saved) =>
            Ok(saved.asJson)
          case Left(_) =>
            Conflict(s"Event creation failed.")
        }
    }


  def endpoints(eventService: EventService[F]): HttpService[F] =
    createPetEndpoint(eventService)
}

object EventEndpoints {
  def endpoints[F[_]: Effect](eventService: EventService[F]): HttpService[F] =
    new EventEndpoints[F].endpoints(eventService)
}
