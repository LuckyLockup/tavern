//package com.ll.service
//
//import akka.Done
//import akka.actor.{ Actor, ActorRef, ActorSystem, Props, Terminated }
//import akka.cluster.Cluster
//import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings, ShardRegion }
//import akka.event.LoggingReceive
//import akka.pattern.ask
//import akka.util.Timeout
//import com.ll.utils.Logging
//
//import scala.concurrent.Future
//
//class WsService(numberOfShards: Int)(implicit actorSystem: ActorSystem) {
//
//  val entityTypeName: String = "Wallet"
//
//  def init: ActorRef = {
//    ClusterSharding(actorSystem).start(
//      typeName = entityTypeName,
//      entityProps = Props(new WsActor()),
//      settings = ClusterShardingSettings(actorSystem),
//      extractEntityId = extractEntityId,
//      extractShardId = extractShardId
//    )
//  }
//
//  // This value should be below the method, otherwise the evaluation throws a NullPointerException
//  private val _wallet: ActorRef = init
//
//  def extractEntityId: ShardRegion.ExtractEntityId = {
//    case CommandEnvelope(id, command) => (id.toLowerCase, command)
//  }
//
//  def extractShardId: ShardRegion.ExtractShardId = {
//    case CommandEnvelope(id, _) => (Math.abs(id.toLowerCase.hashCode) % numberOfShards).toString
//  }
//
//  def wallet() = _wallet
//
//  /**
//    * Attempt to leave the cluster gracefully on sigint
//    */
//  def gracefulShutdown(): Future[Unit] = {
//    val actor = actorSystem.actorOf(GracefulShutdownActor.props(_wallet))
//    implicit val timeout = Timeout(20, concurrent.duration.SECONDS)
//    implicit val ec = actorSystem.dispatcher
//
//    for {
//      _ <- actor ? GracefulShutdownActor.StopShardRegion
//      // ConstructR will shutdown ActorSystem after leaving the cluster
//      // and we make sure stop hook take that into consideration
//      _ <- actorSystem.whenTerminated
//    } yield println("shutdown hook completed!")
//  }
//
//}
//
//class GracefulShutdownActor(shardRegion: ActorRef) extends Actor with Logging {
//
//  import GracefulShutdownActor._
//
//  override def receive: Receive = LoggingReceive {
//
//    case StopShardRegion =>
//      log.info("Initiating graceful shutdown process")
//      context.become(waitingForTermination(sender()))
//      context.watch(shardRegion)
//      shardRegion ! ShardRegion.GracefulShutdown
//  }
//
//  def waitingForTermination(origSender: ActorRef): Receive = {
//    case t: Terminated =>
//      log.info("Shard region terminated. Leaving cluster.")
//      val cluster = Cluster(context.system)
//      cluster.leave(cluster.selfAddress)
//      origSender ! Done
//  }
//}
//
//object GracefulShutdownActor {
//
//  case object StopShardRegion
//
//  def props(shardRegion: ActorRef) =
//    Props(classOf[GracefulShutdownActor], shardRegion)
//}
//
