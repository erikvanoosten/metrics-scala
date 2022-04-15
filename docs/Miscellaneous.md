## Miscellaneous

### Write `MetricFilter` as regular Scala function (Scala 2.11.4 and before)

Since 2.11.5 Scala support SAM (Single Abstract Method) and these implicit conversions are not automatic. In
case you are still on an older version:

You can write a `com.codahale.metrics.MetricFilter` as a regular Scala functions. Start with

    import nl.grons.metrics4.scala.Implicits._

Then wherever you need a `MetricFilter` you can write a function of type `(String, Metric) => Boolean`. E.g.:

    YourApplication.metricRegistry.getCounters((name: String, _: Metric) => name.startsWith("foo"))

or a tad more explicit:

    YourApplication.metricRegistry.getCounters(functionToMetricFilter((name, _) => name.startsWith("foo")))


Previous: [Hdrhistogram](Hdrhistogram.md) Up: [Manual](Manual.md) Next: [Zio](Zio.md)
