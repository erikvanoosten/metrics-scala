## Hdrhistogram

Since 3.4.0 metrics-scala makes it easy to use [hdrhistogram](http://hdrhistogram.org/). Hdrhistogram provides
alternative high quality reservoir implementations which can be used in histograms and timers.
[Hdrhistogram-metrics-reservoir](https://bitbucket.org/marshallpierce/hdrhistogram-metrics-reservoir)
is used to bridge the two worlds.

Use these 2 steps to start using hdrhistogram:

### Step 1: Add dependency

As hdrhistogram-metrics-reservoir is an optional dependency of metrics-scala, you need to include it in your build.
See [mvnrepository](http://mvnrepository.com/artifact/org.mpierce.metrics.reservoir/hdrhistogram-metrics-reservoir/1.1.0)
for instructions for your build tool. For SBT use:

```
libraryDependencies += "org.mpierce.metrics.reservoir" % "hdrhistogram-metrics-reservoir" % "1.1.0"
```

in addition, you should probably override the dependency to HdrHistogram. E.g.:

```
libraryDependencies += "org.hdrhistogram" % "HdrHistogram" % "2.1.6"
```

See the [README](/README.md) to see which version(s) you can use.

### Step 2: override the metric builder

Secondly you need to override the metric builder in your `Instrumented` class:

```scala
object YourApplication {
  /** The application wide metrics registry. */
  val metricRegistry = new com.codahale.metrics.MetricRegistry()
}

trait Instrumented extends nl.grons.metrics.scala.InstrumentedBuilder {
  override lazy protected val metricBuilder = new HdrMetricBuilder(metricBaseName, metricRegistry, resetAtSnapshot = false)
  val metricRegistry = YourApplication.metricRegistry
}
```

### When to use `resetAtSnapshot`?

`HdrMetricBuilder` accepts parameter `resetAtSnapshot`.

Set `resetAtSnapshot` to `true` when you regularly ship snapshots to an external system like Graphite. With this
setting, the reservoirs are reset at every snapshot creation. You should make sure that only the graphite exporter
reads the timer/histogram's values, otherwise you may loose data.

In all other cases, set `resetAtSnapshot` to `false`. Your metrics will then represent a longer history, similar
to Dropwizard's default exponentially decaying reservoir.

See [this article](http://taint.org/2014/01/16/145944a.html) for more information.


Previous: [Instrumenting Actors](/docs/Actors.md) Up: [Manual](/docs/Manual.md)
