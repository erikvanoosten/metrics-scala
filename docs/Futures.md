## Future support

A future can be timed as follows:

```scala
class Example extends DefaultInstrumented {
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
class Example extends DefaultInstrumented {
  private[this] val loading = metrics.timer("loading")

  def fetchRows(): Seq[Row] = ???

  def loadStuff(): Seq[Row] = Future {
    loading.time {
      fetchRows()
    }
  }
}
```

See the scaladoc for `timeFuture` in [Timer](/src/main/scala/nl/grons/metrics4/scala/Timer.scala) for more gotchas.

Previous: [Health check support](HealthCheckManual.md) Up: [Manual](Manual.md) Next: [Instrumenting Actors](Actors.md)
