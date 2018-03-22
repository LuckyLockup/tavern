# Chapter 1. Reactive Programming with RxJava

## Push vs Pull
Pull requests data in some intervals.

```
trait Observable[+A] {
  def subscribe(observer: Observer[A]): Cancellable
}

trait Observer[-A] {
  def onNext(a: A): Future[Ack]
  def onComplete(): Unit
  def onError(Throwable t): Unit
}
```