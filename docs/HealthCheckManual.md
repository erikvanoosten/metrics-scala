# Health Check support

Metrics also has the ability to centralize your service’s health checks with the `metrics-healthchecks` module.

## Health check setup

Simply extend [DefaultInstrumented](/src/main/scala/nl/grons/metrics4/scala/DefaultInstrumented.scala) to get access
to the `healthCheck` builder:

```scala
import nl.grons.metrics4.scala.DefaultInstrumented

class ExampleWorker extends DefaultInstrumented {
  healthCheck("alive") { workerThreadIsActive() }
}
```

This creates and registers a health check named `com.example.ExampleWorker.alive` that is healthy when
`workerThreadIsActive()` returns `true`, and unhealthy with the default message `Health check failed` when it returns
`false`. You can override the unhealthy message as follows:

```scala
healthCheck("alive", unhealthyMessage = "Ouch!") { workerThreadIsActive() }
```

The code block may also return a `Try`, a `Unit`, a `Future`, an `Either` or a
`com.codahale.metrics.health.HealthCheck.Result`. In these cases the `unhealthyMessage` parameter is
always ignored.

For more details see the scaladoc in [CheckedBuilder](/src/main/scala/nl/grons/metrics4/scala/CheckedBuilder.scala).

## Health check names

Each health check has a unique name. In metrics-scala the name starts with a name derived from the *owner class*.
The owner class is the class that extends the `DefaultInstrumented` trait.

The health check name is build from:

  * *Metric base name* By default this is set to the owner class name (e.g., `com.example.proj.auth.SessionStore`).
  * *Name:* A short name describing the health check (e.g., `alive`).

The *metric base name* can be overridden as follows:

```scala
import nl.grons.metrics4.scala.{DefaultInstrumented, MetricName}

class Example extends DefaultInstrumented {
  override lazy val metricBaseName = MetricName("Overridden.Base.Name")
  ....
}
```

Previous: [Manual](Manual.md) Up: [Manual](Manual.md) Next: [Instrumenting Futures](Futures.md)
