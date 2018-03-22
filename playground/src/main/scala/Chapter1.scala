// We need a Scheduler in scope in order to make
// the Observable produce elements when subscribed
import monix.execution.Scheduler.Implicits.global
import monix.reactive._

import concurrent.duration._
import scala.concurrent.Future

object Chapter1 extends App {
  val source: Observable[Long] = Observable.interval(1.second)

  source
    .dump("Here we go: ")
    .subscribe()

}

abstract class ActorRef[-T] extends Future[ActorRef[T]]
//abstract class ActorRef extends java.lang.Comparable[ActorRef]
trait Foo[-T]{
  type Shit <: ActorRef[(T => String) => String] // ---

  def foo(a: Shit): Unit
}

// - contravariant
// + covariant
