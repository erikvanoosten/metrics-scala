## Hdrhistogram

Metrics-scala makes it easy to use [hdrhistogram](http://hdrhistogram.org/). Hdrhistogram provides
alternative high quality reservoir implementations which can be used in histograms and timers.
[Hdrhistogram-metrics-reservoir](https://bitbucket.org/marshallpierce/hdrhistogram-metrics-reservoir)
is used to bridge the two worlds.

Use these steps to start using hdrhistogram:

### Step 1: Add dependency

Include the `metrics4-scala-hdr` artifact in your build. For SBT use:

```
libraryDependencies += "nl.grons" %% "metrics4-scala-hdr" % "4.1.4"
```

See the [README](/README.md) to see which version(s) you can use.

### Step 2: override the metric builder

Then create a trait `Instrumented` that overrides the metric builder:

```scala
import nl.grons.metrics4.scala._

trait Instrumented extends DefaultInstrumented {
  override lazy protected val metricBuilder =
    new HdrMetricBuilder(metricBaseName, metricRegistry, resetAtSnapshot = false)
}
```

### Step 3: use

Your application can now use the `Instrumented` trait instead of `DefaultInstrumented`. For example:

```scala
package com.example.proj
import scala.concurrent.duration._

class UserRepository(db: Database) extends Instrumented {
  val resultCounts: Histogram = metrics.histogram("result-counts")

  def users(): Seq[User] = {
    val results = ???
    resultCounts += results.size()
    results
  }
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


Previous: [Testing](Testing.md) Up: [Manual](Manual.md) Next: [Miscellaneous](Miscellaneous.md)
