package com.ll.utils

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.testkit.{ImplicitSender, TestKit}
import com.ll.domain.auth.UserId
import org.scalatest._
import pureconfig._

import scala.util.Random
import scala.util.control.NonFatal

abstract class Test extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with Logging
  with BeforeAndAfterEach {
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider()))(system)

  private def decider(): Supervision.Decider = {
    case NonFatal(ex) =>
      log.error("Error in stream: ", ex)
      Supervision.Resume
    case ex                      =>
      log.error("Fatal error: " + ex.getMessage)
      Supervision.Stop
  }
  protected val config = loadConfig[TestConfig]("testConfig").fold(failure => {
    throw new RuntimeException(failure.toString) }, identity)

  private var connections = Map.empty[UserId, WsConnection]

  val http: HttpExt = Http()

  def createNewPlayer(): PlayerProbe = {
    val id = Stream.continually(Random.nextInt(10000)).find(id => connections.get(UserId(id)).isEmpty).get
    val userId = UserId(id)
    connections.get(userId).foreach(_.closeConnection())
    val ws = new WsConnection(userId, system, materializer, http, config)
    connections += (userId -> ws)
    PlayerProbe(userId, ws, http, config)
  }

  override def afterAll {
    connections.values.foreach(_.closeConnection())
    TestKit.shutdownActorSystem(system)
  }

  override def afterEach: Unit = {
    connections.values.foreach(_.closeConnection())
    connections = Map.empty
  }
}
