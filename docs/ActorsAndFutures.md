# Actor and Future support

    Actor and Future support is available since metrics-scala v3.0.1 but only in the versions for scala 2.10 or later.

## Actor support

Please see the scaladoc in [ActorMetrics](/src/main/akka/nl/grons/metrics/scala/ActorMetrics.scala).

## Future support

The `FutureMetrics` trait supplies a pair of timer methods, both of which return a `Future[A]` where `A` is the parameterized type of the block passed.  Both expect an `ExecutionContext` to be in implicit scope.

### Timed (Synchronous API)
The first method `timed` was written to be used with a synchronous API and has the following signature:

```scala
def timed[A](block: => A)(implicit ec: ExecutionContext): Future[A]
```

This will execute the provided block in the background using the provided `ExecutionContext`, timing the execution of the block.  It will immediately return the `Future[A]` to the caller.

### Timing (Asynchronous API)
The second method `timing` was written to be used with an asynchronous API and has the following signature:

```scala
def timing[A](block: => Future[A])(implicit ec: ExecutionContext): Future[A]
```

This will start a timer when called, and add an `onComplete` handler to the block-produced `Future[A]` which stops said timer.  This listener closes over the `TimerContext`.  

An important point is that the timer should not be expected to measure *only* the run time, unlike `timed`.  It will also add the time it takes for the work to be scheduled, as well as the time it takes for the `onComplete` listener to be scheduled.  The latter should be an insignificant amount of time.  The former could be a different story.

Please see the scaladoc in [FutureMetrics](/src/main/akka/nl/grons/metrics/scala/FutureMetrics.scala).

Previous: [Health check support](/docs/HealthCheckManual.md) Next: [Manual](/docs/Manual.md)
