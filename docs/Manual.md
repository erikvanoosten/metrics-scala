# Manual

Other manual pages:

* [Health check support](HealthCheckManual.md)
* [Instrumenting Futures](Futures.md)
* [Instrumenting Actors](Actors.md)
* [Testing](Testing.md)
* [Hdrhistogram](Hdrhistogram.md)
* [Miscellaneous](Miscellaneous.md)
* [Zio](Zio.md)
* [Dropwizard metrics documentation](https://dropwizard.github.io/metrics)
* [![Scaladocs](https://www.javadoc.io/badge/nl.grons/metrics4-scala_2.12.svg?color=brightgreen&label=Scaladocs)](https://www.javadoc.io/page/nl.grons/metrics4-scala_2.12/latest/nl/grons/metrics4/scala/DefaultInstrumented.html)

## 1 minute introduction

Metrics-scala provides an easy way to create _metrics_ and _health checks_ in Scala. It builds on Dropwizard's
metrics-core and metrics-healthchecks java libraries.

Simply extend [DefaultInstrumented](/src/main/scala/nl/grons/metrics4/scala/DefaultInstrumented.scala)
and then use the `metrics` and `healthCheck` builders:

```scala
import nl.grons.metrics4.scala.DefaultInstrumented

class Example(db: Database) extends DefaultInstrumented {
  // Define a health check
  healthCheck("alive") { workerThreadIsActive() }

  // Define a timer metric
  private[this] val loading = metrics.timer("loading")

  // Use timer metric
  def loadStuff(): Seq[Row] = loading.time {
    db.fetchRows()
  }
}
```

There are Scala wrappers for each metric type: [gauge](#gauges), [counter](#counters), [histogram](#histograms),
[meter](#meters) and [timer](#timers). These are described below.

*Health check* support is described further at [Health check support](HealthCheckManual.md).

There are also helper methods to instrument [Futures](Futures.md) and [Actors](Actors.md).

For more information on (JMX) reporters and other aspects of Metrics, please see the Java api in the
[Metrics documentation](http://metrics.dropwizard.io).

## Gauges

A gauge is the simplest metric type. It just returns a value. If, for example, your application has a value which is
maintained by a third-party library, you can easily expose it by registering a Gauge instance which returns that value:

```scala
package com.example.proj.auth
import nl.grons.metrics4.scala.DefaultInstrumented

class SessionStore(cache: Cache) extends DefaultInstrumented {
  metrics.gauge("cache-evictions") {
    cache.getEvictionsCount()
  }
}
```

This will create a new gauge named `com.example.proj.auth.SessionStore.cache-evictions` which will return the number
of evictions from the cache.

Note: when a Gauge is created from an Actor with restart behavior, trait `ActorInstrumentedLifeCycle` should also be
mixed in. See [Instrumenting Actors](Actors.md) for more information.

Note: if your application has some kind of restarting behavior and you want to re-register its gauges during the
restart, you should invoke `metrics.unregisterGauges()` at the beginning of the restart. This will un-register all
gauges that were registered from the current instance (or more precisely from the `MetricBuilder` instance which was
created by the `DefaultInstrumented` trait). Trait `ActorInstrumentedLifeCycle` automates this for actors.

## Cached gauges

In case the gauge method is expensive to calculate you can use a cached gauge. A cached gauge retains the calculated
value for a given duration.

```scala
package com.example.proj
import nl.grons.metrics4.scala.DefaultInstrumented
import scala.concurrent.duration._

class UserRepository(db: Database) extends DefaultInstrumented {
  metrics.cachedGauge("row-count", 5.minutes) {
    db.usersRowCount()
  }
}
```

This will create a new gauge named `com.example.proj.UserRepository.row-count` which will return the results of the
database query. Once the value is retrieved, it will be retained for 5 minutes. Only when the gauge's value is
requested after these 5 minutes, the database query is executed again.

## Counters

A counter is a simple incrementing and decrementing 64-bit integer:

```scala
val evictions: Counter = metrics.counter("evictions")
evictions += 1
evictions -= 2
```

All `Counter` metrics start out at 0.

## Concurrency counters

Counters have build in support for counting the number of concurrently running tasks:

```scala
val someWorkConcurrencyCounter: Counter = metrics.counter("concurrency.counter")
val result = someWorkConcurrencyCounter.countConcurrency {
  someWork()
}
```

## Histograms

A `Histogram` measures the distribution of values in a stream of data: e.g., the number of results returned by a search:

```scala
val resultCounts: Histogram = metrics.histogram("result-counts")
resultCounts += results.size()
```

`Histogram` metrics allow you to measure not just easy things like the min, mean, max, and standard deviation of values,
but also [quantiles](http://en.wikipedia.org/wiki/Quantile) like the median or 95th percentile.

Traditionally, the way the median (or any other quantile) is calculated is to take the entire data set, sort it, and
take the value in the middle (or 1% from the end, for the 99th percentile). This works for small data sets, or batch
processing systems, but not for high-throughput, low-latency services.

The solution for this is to sample the data as it goes through. By maintaining a small, manageable reservoir which is
statistically representative of the data stream as a whole, we can quickly and easily calculate quantiles which are
valid approximations of the actual quantiles. This technique is called *reservoir sampling*.

Metrics provides a number of different `Reservoir` implementations, each of which is useful.

### Uniform Reservoirs

A histogram with a uniform reservoir produces quantiles which are valid for the entirely of the histogram’s lifetime. It
will return a median value, for example, which is the median of all the values the histogram has ever been updated with.
It does this by using an algorithm called [Vitter’s R](http://www.cs.umd.edu/~samir/498/vitter.pdf), which randomly
selects values for the reservoir with linearly-decreasing probability.

Use a uniform histogram when you’re interested in long-term measurements. Don’t use one where you’d want to know if the
distribution of the underlying data stream has changed recently.

### Exponentially Decaying Reservoirs

A histogram with an exponentially decaying reservoir produces quantiles which are representative of (roughly) the last
five minutes of data. It does so by using a
[forward-decaying priority reservoir](http://dimacs.rutgers.edu/~graham/pubs/papers/fwddecay.pdf) with an exponential
weighting towards newer data. Unlike the uniform reservoir, an exponentially decaying reservoir represents recent data,
allowing you to know very quickly if the distribution of the data has changed. Timers use histograms with exponentially
decaying reservoirs by default.

### Sliding Window Reservoirs

A histogram with a sliding window reservoir produces quantiles which are representative of the past `N` measurements.

### Sliding Time Window Reservoirs

A histogram with a sliding time window reservoir produces quantiles which are strictly representative of the past `N`
seconds (or other time period).

** Warning**: While `SlidingTimeWindowReservoir` is easier to understand than `ExponentiallyDecayingReservoir`, it is
not bounded in size, so using it to sample a high-frequency process can require a significant amount of memory. Because
it records every measurement, it’s also the slowest reservoir type.

TODO: show how to use a different reservoir in a histogram.

## Meters

A meter measures the *rate* at which a set of events occur:

```scala
val getRequests: Meter = metrics.meter("get-requests")
getRequests.mark()
getRequests.mark(requests.size())
```

This meter measures the number of 'getRequests' per second.

Meters measure the rate of the events in a few different ways. The mean rate is the average rate of events. It’s
generally useful for trivia, but as it represents the total rate for your application’s entire lifetime (e.g., the total
number of requests handled, divided by the number of seconds the process has been running), it doesn’t offer a sense of
recency. Luckily, meters also record three different *exponentially-weighted moving average* rates: the 1-, 5-, and
15-minute moving averages. Just like the Unix load averages visible in uptime or top.

### Metering exceptions of partial functions

Metrics-scala allows you to convert any partial function into another partial function that meters exceptions during its
invocations. See the scaladoc of [Meter.exceptionMarkerPF](/src/main/scala/nl/grons/metrics4/scala/Meter.scala#L90).

## Timers

A timer is basically a histogram of the duration of a type of event and a meter of the rate of its occurrence.

```scala
val timer: Timer = metrics.timer("get-requests")

timer.time {
    // handle request
}
```

**Note**: Elapsed times for its events are measured internally in nanoseconds, using Java’s high-precision
`System.nanoTime()` method. Its precision and accuracy vary depending on operating system and hardware.

### Timing partial functions

Metrics-scala allows you to convert any partial function into another partial function that times each invocation. See
the scaladoc of [Timer.timePF](/src/main/scala/nl/grons/metrics4/scala/Timer.scala#L69).

## Metric names and the metrics builder

Each metric has a unique metric name. In metrics-scala the name starts with a name derived from the *owner class*.
The owner class is the class that extends the `DefaultInstrumented` trait.

The metric name is build from:

* *Metric base name* By default this is set to the owner class name (e.g., `com.example.proj.auth.SessionStore`).
* *Name:* A short name describing the metric’s purpose (e.g., `session-count`).

The *metric base name* can be overridden as follows:

```scala
import nl.grons.metrics4.scala.{DefaultInstrumented, MetricName}

class Example extends DefaultInstrumented {
  override lazy val metricBaseName = MetricName("overridden.base.name")
  ...
}
```

The factory methods `metrics.gauge`, `metrics.counter`, `metrics.histogram`, `metrics.meter` and `metrics.timer` also
have a `scope` parameter. This parameter has been deprecated and will be removed in metrics-scala 5.0.0. If
you find code that uses it, then refactor by concatenating `scope` to the `name` with a dot as separator.
For example: `metrics.timer("name", "scope")` is 100% equivalent to `metrics.timer("name.scope")`.

## About `DefaultInstrumented`

`DefaultInstrumented` uses the Dropwizard 1.0.0+ application convention of storing the metric registry in metric-core's
`SharedMetricRegistries` under the name `"default"`. It extends this by also storing a health check registry in the
metric-healthcheck's `SharedHealthCheckRegistries` under the same name.

## Custom registries

If you wish to use a different metric registry or health check registry you can write a custom `Instrumented` trait.
For metrics support the trait should extend
[InstrumentedBuilder](/src/main/scala/nl/grons/metrics4/scala/InstrumentedBuilder.scala), for health check support
the trait should extend [CheckedBuilder](/src/main/scala/nl/grons/metrics4/scala/CheckedBuilder.scala).

Here is an example that supports both:

```scala
object YourApplication {
  /** The application wide metrics registry. */
  val metricRegistry = new com.codahale.metrics.MetricRegistry()
  /** The application wide health check registry. */
  val healthChecksRegistry = new com.codahale.metrics.health.HealthCheckRegistry()
}

import nl.grons.metrics.scala._
trait Instrumented extends InstrumentedBuilder with CheckedBuilder {
  lazy val metricRegistry = YourApplication.metricRegistry
  lazy val registry = YourApplication.healthChecksRegistry
}
```

Now use `Instrumented` in your code instead of `DefaultInstrumented`.

Next: [Health check support](HealthCheckManual.md)
