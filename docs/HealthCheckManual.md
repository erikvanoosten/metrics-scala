# Health Check support

    Health check support is available since metrics-scala v3.0.3.

Metrics also has the ability to centralize your service’s health checks with the metrics-healthchecks module.

Metrics-healthchecks requires health checks to be registered in an application wide `HealthCheckRegistry`. Metrics-scala hides use of it, but you do need to create an `Checked` trait that refers to that registry. Your `Checked` needs to extends `CheckedBuilder`. (See also [About Checked](#about-checked-and-instrumented) below.)

```scala
object YourApplication {
  /** The application wide health check registry. */
  val healthChecksRegistry = new com.codahale.metrics.health.HealthCheckRegistry();
}
trait Checked extends nl.grons.metrics.scala.CheckedBuilder {
  val healthCheckRegistry = Application.healthCheckRegistry
}
```

Now you can easily create and register health checks by using the `healthCheck` factory method:

```scala
package com.example

class Example(db: Database) extends Checked {
  healthCheck("database") { db.isConnected }
}
```

This creates and registers a health check named `com.example.Example.database` that is healthy when `db.isConnected` returns `true`, and unhealthy with the default message `Health check failed` when it returns `false`. You can override the unhealthy message as follows:

```scala
healthCheck("database", unhealthyMessage = "Ouch!") { db.isConnected }
```

The code block may also return an `Either` or a `com.codahale.metrics.health.HealthCheck.Result`. In these cases the `unhealthyMessage` is always ignored.

For more details see the scaladoc in [CheckedBuilder](/src/main/scala/nl/grons/metrics/scala/CheckedBuilder.scala).

## About `Checked` and `Instrumented`

Although all Metrics-scala documentation refers to the `Checked` trait (as created in the text above), you are free to chose the name, or in fact not to use it at all. It is also possible to directly extend `CheckedBuilder` and provide an implementation of `healthCheckRegistry` in every class.

There is also opportunity to combine your `Instrumented` and `Checked` in a single trait:

```scala
import nl.grons.metrics.scala._

object YourApplication {
  /** The application wide metrics registry. */
  val metricRegistry = new com.codahale.metrics.MetricRegistry()
  /** The application wide health check registry. */
  val healthCheckRegistry = new com.codahale.metrics.health.HealthCheckRegistry();
}

trait Instrumented extends InstrumentedBuilder with CheckedBuilder {
  val metricRegistry = YourApplication.metricRegistry
  val healthCheckRegistry = YourApplication.healthCheckRegistry
}
```

In this variant `Instrumented` supports both building metrics and health checks.

Previous: [Manual](/docs/Manual.md) Next: [Instrumenting Actors and Futures](/docs/ActorsAndFutures.md)

