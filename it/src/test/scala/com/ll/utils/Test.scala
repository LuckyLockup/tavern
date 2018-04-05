package com.ll.utils

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit}
import com.ll.domain.auth.UserId
import org.scalatest._
import pureconfig._

abstract class Test extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with Logging{
  implicit val materializer = ActorMaterializer()

  protected val config = loadConfig[TestConfig]("testConfig").fold(failure => {
    throw new RuntimeException(failure.toString) }, identity)

  private var connections = Map.empty[UserId, WsConnection]

  val http: HttpExt = Http()

  def createNewPlayer(userId: UserId) = {
    connections.get(userId).foreach(_.closeConnection())
    val ws = new WsConnection(userId, system, materializer, http, config)
    connections += (userId -> ws)
    Player(userId, ws, http, config)
  }

  override def afterAll {
    connections.values.foreach(_.closeConnection())
    TestKit.shutdownActorSystem(system)
  }
}
