## Miscellaneous

### Write `MetricFilter` as regular Scala function

Since 3.5.1 you can write a `com.codahale.metrics.MetricFilter` as a regular Scala functions. Start with

    import nl.grons.metrics.scala.Implicits._

Then wherever you need a `MetricFilter` you can write a function of type `(String, Metric) => Boolean`. E.g.:

    YourApplication.metricRegistry.getCounters((name: String, _: Metric) => name.startsWith("foo"))

or a tad more explicit:

    YourApplication.metricRegistry.getCounters(functionToMetricFilter((name, _) => name.startsWith("foo")))


Previous: [Hdrhistogram](/docs/Hdrhistogram.md) Up: [Manual](/docs/Manual.md)
