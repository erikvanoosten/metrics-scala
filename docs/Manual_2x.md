**WARNING:** This document applies to metrics-scala 2.x only. Please use [Usage](/docs/Usage.md) for metrics 3.x.

# Usage (version 2.x)

# 30 seconds introduction

Metrics-scala provides the ``Instrumented`` trait for Scala applications. This
trait gives you the metrics builder `metrics`.

For example, here is how to create and use a timer:

```scala
import com.yammer.metrics.scala.Instrumented

class Example(db: Database) extends Instrumented {
  private[this] val loading = metrics.timer("loading")

  def loadStuff(): Seq[Row] = loading.time {
    db.fetchRows()
  }
}
```

There are Scala wrappers for each metric type: `gauge`, `counter`, `histogram`, `meter` and `timer`. These are described below.

There is no special support for Health Checks, JMX or other Reporters. For more information on these please see the Metrics 2.x
[documentation in the Way Back Machine](http://web.archive.org/web/20120925003800/http://metrics.codahale.com/manual/core/)
or read it directly from [Metrics 2.2.0 git branch](https://github.com/codahale/metrics/tree/v2.2.0/docs/source/manual).

# Gauges

A gauge is the simplest metric type. It just returns a value. If, for example, your application has a value which is maintained by a third-party library, you can easily expose it by registering a Gauge instance which returns that value:

```scala
import com.yammer.metrics.scala.Instrumented

class SessionStore(cache: Cache) extends Instrumented {
  metrics.gauge("cache-evictions") {
    cache.getEvictionsCount()
  }
}
```

This will create a new gauge named `com.example.proj.auth.SessionStore.cache-evictions` which will return the number of evictions from the cache.

# Counters

A counter is a simple incrementing and decrementing 64-bit integer:

```scala
val evictions: Counter = metrics.counter
evictions += 1
evictions -= 2
```

All `Counter` metrics start out at 0.

# Histograms

A `Histogram` measures the distribution of values in a stream of data: e.g., the number of results returned by a search:

```scala
val resultCounts: Histogram = metrics.histogram("result-counts")
resultCounts += results.size()
```

`Histogram` metrics allow you to measure not just easy things like the min, mean, max, and standard deviation of values, but also [quantiles](http://en.wikipedia.org/wiki/Quantile) like the median or 95th percentile.

Traditionally, the way the median (or any other quantile) is calculated is to take the entire data set, sort it, and take the value in the middle (or 1% from the end, for the 99th percentile). This works for small data sets, or batch processing systems, but not for high-throughput, low-latency services.

The solution for this is to sample the data as it goes through. By maintaining a small, manageable sample which is statistically representative of the data stream as a whole, we can quickly and easily calculate quantiles which are valid approximations of the actual quantiles. This technique is called *reservoir sampling*.

Metrics provides two types of histograms: `uniform` (the default) and `biased`:

## Uniform Histograms

A uniform histogram produces quantiles which are valid for the entirely of the histogram’s lifetime. It will return a median value, for example, which is the median of all the values the histogram has ever been updated with. It does this by using an algorithm called [Vitter’s R](http://www.cs.umd.edu/~samir/498/vitter.pdf)), which randomly selects values for the sample with linearly-decreasing probability.

Use a uniform histogram when you’re interested in long-term measurements. Don’t use one where you’d want to know if the distribution of the underlying data stream has changed recently.

## Biased Histograms

A biased histogram produces quantiles which are representative of (roughly) the last five minutes of data. It does so by using a [forward-decaying priority sample](http://www.research.att.com/people/Cormode_Graham/library/publications/CormodeShkapenyukSrivastavaXu09.pdf) with an exponential weighting towards newer data. Unlike the uniform histogram, a biased histogram represents *recent data*, allowing you to know very quickly if the distribution of the data has changed. Timers use biased histograms.

Create a biased histogram as follows:

```scala
val resultCounts: Histogram = metrics.histogram("result-counts", biased = true)
```

# Meters

A meter measures the rate at which a set of events occur:

```scala
val getRequests: Meter = metrics.meter("get-requests", "requests")
getRequests.mark()
getRequests.mark(requests.size())
```

A meter requires two additional pieces of information besides the name: the *event type* and the *rate unit*.
The event type simply describes the type of events which the meter is measuring. In the above case, the meter is measuring proxied requests, and so its event type is `"requests"`.
The rate unit is the unit of time denominating the rate. In the above case, the meter is measuring the number of requests in each second, because the default rate unit is `SECONDS`. When combined, the meter is measuring requests per second.

To change the *rate unit*, specify the `unit` parameter as follows:

```scala
val getRequests: Meter = metrics.meter("get-requests", "requests", unit = TimeUnit.MINUTES)
```

Meters measure the rate of the events in a few different ways. The *mean* rate is the average rate of events. It’s generally useful for trivia, but as it represents the total rate for your application’s entire lifetime (e.g., the total number of requests handled, divided by the number of seconds the process has been running), it doesn’t offer a sense of recency. Luckily, meters also record three different *exponentially-weighted moving average* rates: the 1-, 5-, and 15-minute moving averages. Just like the Unix load averages visible in uptime or top.

# Timers

A timer is basically a histogram of the duration of a type of event and a meter of the rate of its occurrence.

```scala
val timer: Timer = metrics.timer("get-requests")

timer.time {
    // handle request
}
```

A timer requires two additional pieces of information besides the name: the *duration unit* and the *rate unit*. The duration unit is the unit of time in which the durations of events will be measured. In the above example, the duration unit is the default `MILLISECONDS`, meaning the timed event’s duration will be measured in milliseconds. The rate unit in the above example is the default `SECONDS`, meaning the rate of the timed event is measured in calls/sec.

To change the *duration unit* or *rate unit*, specify the `durationUnit` and/or `unit` parameters as follows:

```scala
val timer: Timer = metrics.timer("get-requests", durationUnit = TimeUnit.SECONDS, unit = TimeUnit.MINUTES)
```

**Note**: Regardless of the display duration unit of a timer, elapsed time for its events is measured internally in nanoseconds, using Java’s high-precision `System.nanoTime()` method.

# Metric names and Instrumented

Each metric has a unique metric name. In metrics-scala the name partly derived from the owner class. The owner class is the class that extends the `Instrumented` trait. The name is build from:

* *Group:* The top-level grouping of the metric. This is set to the owner’s package name (e.g., com.example.proj.auth).
* *Type:* The second-level grouping of the metric. This defaults to the owner’s class name (e.g., SessionStore).
* *Name:* A short name describing the metric’s purpose (e.g., session-count).
* *Scope:* An optional name describing the metric’s scope. Useful for when you have multiple instances of a class.

The factory methods `metrics.gauge`, `metrics.counter`, `metrics.histogram`, `metrics.meter` and `metrics.timer` all accept a `scope` argument. Be default the scope is not used.
