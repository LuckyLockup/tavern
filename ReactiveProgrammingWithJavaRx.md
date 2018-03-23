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

## Async vs sync
sync is ok:
 - data is in memory.

## Other:
Duality (Iterable vs Observable)

Pro's to use Observable insteaf of Future:
 - stream
 - multiple values
 - composition
 - single
 - completable 