## Future support

    Future support is available in all builds since metrics-scala 3.2.0 and in Akka builds since 3.0.1.

The `FutureMetrics` trait supplies a pair of timer methods, both of which return a `Future[A]` where `A` is the parameterized type of the block passed.  Both expect an `ExecutionContext` to be in implicit scope.

### Timed (Synchronous API)

The first method `timed` is written to be used with a synchronous API and has the following signature:

```scala
def timed[A](block: => A)(implicit ec: ExecutionContext): Future[A]
```

This executes the provided block in the background using the provided `ExecutionContext`, timing the execution of the block. It immediately returns the `Future[A]` to the caller.

### Timing (Asynchronous API), from version 3.2.0

The second method `timing` is written to be used with an asynchronous API and has the following signature:

```scala
def timing[A](block: => Future[A])(implicit ec: ExecutionContext): Future[A]
```

This starts a timer when called, and adds an `onComplete` handler to the block-produced `Future[A]` which stops said timer. This listener closes over the `TimerContext`.

An important point is that the timer does not measure *only* the run time, unlike `timed`. It also adds the time it takes for the work to be scheduled, as well as the time it takes for the `onComplete` listener to be scheduled. The latter should be an insignificant amount of time. The former could be a different story.

Please see the scaladoc in [FutureMetrics](/src/main/scala/nl/grons/metrics/scala/FutureMetrics.scala).

Previous: [Health check support](/docs/HealthCheckManual.md) Next: [Instrumenting Actors](/docs/Actors.md)
