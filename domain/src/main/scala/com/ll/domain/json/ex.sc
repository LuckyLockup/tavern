import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json, ObjectEncoder}
import io.circe.generic.semiauto._
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.parser._

sealed trait Foo
object Foo {
  case class Foo1(foo1: String) extends Foo
  case class Foo2(foo2: String) extends Foo

  implicit lazy val FooDecoder: Decoder[Foo] = new Decoder[Foo] {
    final def apply(c: HCursor): Decoder.Result[Foo] = {
      def decode(messageType: String, payload: Json): Decoder.Result[Foo] = messageType match {
        case "Foo1" => payload.as[Foo.Foo1](deriveDecoder[Foo.Foo1])
        case "Foo2" => payload.as[Foo.Foo2](deriveDecoder[Foo.Foo2])
      }

      for {
        messageType <- c.downField("type").as[String]
        payload <- c.downField("payload").focus.toRight(DecodingFailure("payload field is not present", Nil))
        in <- decode(messageType, payload)
      } yield in
    }
  }
}

case class Bar(foo1: Foo)

object Bar {
  implicit lazy val barDecoder: Decoder[Bar] = deriveDecoder[Bar]
}




parse("""
  |{ "foo1": {
  |  "type" : "Foo1",
  |    "payload": {
  |      "foo1": "bar"
  |    }
  |  }
  |}
""".stripMargin)
  .flatMap(json => json.as[Bar])
