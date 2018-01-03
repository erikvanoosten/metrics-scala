## Future support

Since metrics-scala 3.5.4 a future is timed as follows:

```scala
class Example extends Instrumented {
  private[this] val loading = metrics.timer("loading")

  def asyncFetchRows(): Future[Seq[Row]] = ???

  def loadStuff(): Future[Seq[Row]] = loading.timeFuture {
    asyncFetchRows()
  }
}
```

*Know what you measure*

Method `timeFuture` does not measure the execution time of the `future`, instead it measures wall clock time from
the moment of calling until the future is completed. If the future was already executed, it will only measure how long
it takes to schedule stopping the timer. If the future was not yet executed, it will measure the time it takes to
schedule the future for execution in addition to the actual execution.

If you only need to measure the execution time, then use a timer inside the future's execution. For example:

```scala
class Example extends Instrumented {
  private[this] val loading = metrics.timer("loading")

  def fetchRows(): Seq[Row] = ???

  def loadStuff(): Seq[Row] = Future {
    loading.time {
      fetchRows()
    }
  }
}
```

See also the scaladoc for `timeFuture` in [Timer](/src/main/scala/nl/grons/metrics/scala/Timer.scala).

## Future support before version 3.5.4

Since version 3.5.4 `FutureMetrics` has been deprecated. It was removed in 4.0.0.

For method `FutureMetrics.timing` please rewrite your code as follows:

```scala
// Before:
class Example extends Instrumented with FutureMetrics {
  timing("someMetricName") { ... some future ... }
}

// After:
class Example extends Instrumented {
  val someTimer = metrics.timer("someMetricName")
  someTimer.timeFuture { ... some future ... }
}
```

For method `FutureMetrics.timed` please rewrite your code as follows:

```scala
// Before:
class Example extends Instrumented with FutureMetrics {
  timed("someMetricName") { ... some action ... }
}

// After:
class Example extends Instrumented {
  private[this] val someTimer = metrics.timer("someMetricName")
  Future {
    someTimer.time { ... some action ... }
  }
}
```

For versions before version 3.5.4: see the scaladoc in `FutureMetrics`.

Previous: [Health check support](HealthCheckManual.md) Up: [Manual](Manual.md) Next: [Instrumenting Actors](Actors.md)
