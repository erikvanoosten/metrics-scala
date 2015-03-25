## Future support

    Future support is available in all builds since metrics-scala 3.2.0 and in Akka builds since 3.0.1.

The `FutureMetrics` trait supplies a pair of timer methods, both of which return a `Future[A]` where `A` is the parameterized type of the block passed.  Both expect an `ExecutionContext` to be in implicit scope.

`FutureMetrics` can be mixed in with the `Instrumented` class (see [Manual](/docs/Manual.md)) as follows:

```scala
object YourApplication { ... }
trait Instrumented extends nl.grons.metrics.scala.InstrumentedBuilder with FutureMetrics {
  val metricRegistry = YourApplication.metricRegistry
}
```

Alternatively, you can mixin `FutureMetrics` only with the classes that need Future support:

```scala
class Example(db: Database) extends Instrumented with FutureMetrics {
  ...
}
```


### Timed (Synchronous API)

The first method `timed` is written to be used with a synchronous API and has the following signature:

```scala
def timed[A](block: => A)(implicit ec: ExecutionContext): Future[A]
```

This executes the provided block in the background using the provided `ExecutionContext`, timing the execution of the block. It immediately returns the `Future[A]` to the caller.

### Timing (Asynchronous API), from version 3.2.0

The second method `timing` is written to be used with an asynchronous API and has the following signature:

```scala
def timing[A](future: => Future[A])(implicit ec: ExecutionContext): Future[A]
```

This starts a timer when called. The timer stops when the given future completes.

An important point is that the timer does not measure the exact execution time of the `future`, unlike `timed`. The future might not have been scheduled, or it could have been completed the moment `timing` is called. The `onComplete` listener also needs to be scheduled. If you need exact timings, please make sure to use a timer inside the future's execution.

Please see the scaladoc in [FutureMetrics](/src/main/scala/nl/grons/metrics/scala/FutureMetrics.scala).

Previous: [Health check support](/docs/HealthCheckManual.md) Up: [Manual](/docs/Manual.md) Next: [Instrumenting Actors](/docs/Actors.md)
