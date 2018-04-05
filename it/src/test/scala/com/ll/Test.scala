package com.ll

import akka.stream.ActorMaterializer
import pureconfig._
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}

import org.scalatest._

abstract class Test extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with FunSuiteLike with Matchers with BeforeAndAfterAll with Logging{
  implicit val materializer = ActorMaterializer()

  val config = loadConfig[TestConfig]("serverconfig")

  def createWsConnection(userId: Int): WsConnection = {
    new WsConnection(userId, system, materializer)
  }


  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}
