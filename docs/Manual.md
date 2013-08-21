# Manual

Other manual pages:

* [Health check support](/docs/HealthCheckManual.md)
* [Instrumenting Actors and Futures](/docs/ActorsAndFutures.md)
* [Coda Hale metrics documentation](http://metrics.codahale.com)

## 1 minute introduction

Metrics-scala provides an easy way to create metrics in Scala.

Metrics-core requires metrics to be registered in an application wide `MetricRegistry`. Metrics-scala hides use of it, but you do need to create an `Instrumented` trait that refers to that registry. Your `Instrumented` needs to extends `InstrumentedBuilder`. (See also [About Instrumented](#about-instrumented) below.)

```scala
object YourApplication {
  /** The application wide metrics registry. */
  val metricRegistry = new com.codahale.metrics.MetricRegistry()
}
trait Instrumented extends nl.grons.metrics.scala.InstrumentedBuilder {
  val metricRegistry = YourApplication.metricRegistry
}
```

Now you can easily create metrics by using the `metrics` metrics builder:

```scala
class Example(db: Database) extends Instrumented {
  private[this] val loading = metrics.timer("loading")

  def loadStuff(): Seq[Row] = loading.time {
    db.fetchRows()
  }
}
```

There are Scala wrappers for each metric type: [gauge](#gauges), [counter](#counters), [histogram](#histograms), [meter](#meters) and [timer](#timers). These are described below.

*Health check* support is described at [Health check support](/docs/HealthCheckManual.md).

In Scala 2.10 you can also instrument *Actors* and *Futures*. This is described at [Instrumenting Actors and Futures](/docs/ActorsAndFutures.md).

There is no scala support for JMX or other Reporters. For more information on Metrics 3.x, please see the [documentation](http://metrics.codahale.com).


## Gauges

A gauge is the simplest metric type. It just returns a value. If, for example, your application has a value which is maintained by a third-party library, you can easily expose it by registering a Gauge instance which returns that value:

```scala
class SessionStore(cache: Cache) extends Instrumented {
  metrics.gauge("cache-evictions") {
    cache.getEvictionsCount()
  }
}
```

This will create a new gauge named `com.example.proj.auth.SessionStore.cache-evictions` which will return the number of evictions from the cache.

## Counters

A counter is a simple incrementing and decrementing 64-bit integer:

```scala
val evictions: Counter = metrics.counter
evictions += 1
evictions -= 2
```

All `Counter` metrics start out at 0.

## Histograms

A `Histogram` measures the distribution of values in a stream of data: e.g., the number of results returned by a search:

```scala
val resultCounts: Histogram = metrics.histogram("result-counts")
resultCounts += results.size()
```

`Histogram` metrics allow you to measure not just easy things like the min, mean, max, and standard deviation of values, but also [quantiles](http://en.wikipedia.org/wiki/Quantile) like the median or 95th percentile.

Traditionally, the way the median (or any other quantile) is calculated is to take the entire data set, sort it, and take the value in the middle (or 1% from the end, for the 99th percentile). This works for small data sets, or batch processing systems, but not for high-throughput, low-latency services.

The solution for this is to sample the data as it goes through. By maintaining a small, manageable reservoir which is statistically representative of the data stream as a whole, we can quickly and easily calculate quantiles which are valid approximations of the actual quantiles. This technique is called *reservoir sampling*.

Metrics provides a number of different `Reservoir` implementations, each of which is useful.

### Uniform Reservoirs

A histogram with a uniform reservoir produces quantiles which are valid for the entirely of the histogram’s lifetime. It will return a median value, for example, which is the median of all the values the histogram has ever been updated with. It does this by using an algorithm called [Vitter’s R](http://www.cs.umd.edu/~samir/498/vitter.pdf), which randomly selects values for the reservoir with linearly-decreasing probability.

Use a uniform histogram when you’re interested in long-term measurements. Don’t use one where you’d want to know if the distribution of the underlying data stream has changed recently.

### Exponentially Decaying Reservoirs

A histogram with an exponentially decaying reservoir produces quantiles which are representative of (roughly) the last five minutes of data. It does so by using a [forward-decaying priority reservoir](http://dimacs.rutgers.edu/~graham/pubs/papers/fwddecay.pdf) with an exponential weighting towards newer data. Unlike the uniform reservoir, an exponentially decaying reservoir represents recent data, allowing you to know very quickly if the distribution of the data has changed. Timers use histograms with exponentially decaying reservoirs by default.

### Sliding Window Reservoirs

A histogram with a sliding window reservoir produces quantiles which are representative of the past `N` measurements.

### Sliding Time Window Reservoirs

A histogram with a sliding time window reservoir produces quantiles which are strictly representative of the past `N` seconds (or other time period).

** Warning**: While `SlidingTimeWindowReservoir` is easier to understand than `ExponentiallyDecayingReservoir`, it is not bounded in size, so using it to sample a high-frequency process can require a significant amount of memory. Because it records every measurement, it’s also the slowest reservoir type.

TODO: show how to use a different reservoir in a histogram.

## Meters

A meter measures the *rate* at which a set of events occur:

```scala
val getRequests: Meter = metrics.meter("get-requests", "requests")
getRequests.mark()
getRequests.mark(requests.size())
```

This meter measures the number of `requests` (second parameter) per second.

Meters measure the rate of the events in a few different ways. The mean rate is the average rate of events. It’s generally useful for trivia, but as it represents the total rate for your application’s entire lifetime (e.g., the total number of requests handled, divided by the number of seconds the process has been running), it doesn’t offer a sense of recency. Luckily, meters also record three different *exponentially-weighted moving average* rates: the 1-, 5-, and 15-minute moving averages. Just like the Unix load averages visible in uptime or top.

### Metering exceptions of partial functions

Metrics-scala allows you to convert any partial function into another partial function that meters exceptions during its invocations. See the scaladoc of [Meter.exceptionMarkerPF](/src/main/scala/nl/grons/metrics/scala/Meter.scala#L90).

Partial function support is available since metrics-scala v3.0.1.

## Timers

A timer is basically a histogram of the duration of a type of event and a meter of the rate of its occurrence.

```scala
val timer: Timer = metrics.timer("get-requests")

timer.time {
    // handle request
}
```

**Note**: Elapsed times for its events are measured internally in nanoseconds, using Java’s high-precision `System.nanoTime()` method. Its precision and accuracy vary depending on operating system and hardware.

### Timing partial functions

Metrics-scala allows you to convert any partial function into another partial function that times each invocation. See the scaladoc of [Timer.timePF](/src/main/scala/nl/grons/metrics/scala/Timer.scala#L69).

Partial function support is available since metrics-scala v3.0.1.

## Metric names and the metrics builder

Each metric has a unique metric name. In metrics-scala the name partly derived from the *owner class*. The owner class is the class that extends the `Instrumented` trait you defined earlier.

The metric name is build from:

* *Group:* The top-level grouping of the metric. This is set to the owner’s package name (e.g., com.example.proj.auth).
* *Type:* The second-level grouping of the metric. This defaults to the owner’s class name (e.g., SessionStore).
* *Name:* A short name describing the metric’s purpose (e.g., session-count).
* *Scope:* An optional name describing the metric’s scope. Useful for when you have multiple instances of a class.

The factory methods `metrics.gauge`, `metrics.counter`, `metrics.histogram`, `metrics.meter` and `metrics.timer` all accept a `scope` argument. Be default the scope is not used.

## About `Instrumented`

Although all Metrics-scala documentation refers to the `Instrumented` trait (as created in the introduction above), you are free to chose the name, or in fact not to use it at all. It is also possible to directly extend `InstrumentedBuilder` and provide an implementation of `metricRegistry` in every class.

If health checks are also needed, you can combine `Instrumented` with the `Checked` trait. See [Checked and Instrumented](/docs/HealthCheckManual.md#about-checked-and-instrumented).

Next: [Health check support](/docs/HealthCheckManual.md)
