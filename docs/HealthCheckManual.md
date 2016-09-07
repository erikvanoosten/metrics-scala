# Health Check support

Metrics also has the ability to centralize your service’s health checks with the `metrics-healthchecks` module.

## Health check setup in version 3.5.5 and later

Since version 3.5.5 you can simply extend
[DefaultInstrumented](/src/main/scala/nl/grons/metrics/scala/DefaultInstrumented.scala) to get access to the
`healthCheck` builder:

```scala
class ExampleWorker extends nl.grons.metrics.scala.DefaultInstrumented {
  healthCheck("alive") { workerThreadIsActive() }
}
```

Note that the documentation below uses `Checked` instead of `DefaultInstrumented`.

## Health checks setup in version 3.0.3 up to 3.5.4

Metrics-healthchecks requires health checks to be registered in an application wide `HealthCheckRegistry`. Metrics-scala
hides use of it, but you do need to create an `Checked` trait that refers to that registry. Your `Checked` needs to
extends `CheckedBuilder`. (See also [About Checked](#about-checked-and-instrumented) below.)

```scala
object YourApplication {
  /** The application wide health check registry. */
  val healthChecksRegistry = new com.codahale.metrics.health.HealthCheckRegistry()
}
trait Checked extends nl.grons.metrics.scala.CheckedBuilder {
  val registry = YourApplication.healthChecksRegistry
}
```

## Building health checks

With either setup from above you can easily create and register health checks by using the `healthCheck` builder:

```scala
package com.example

class ExampleWorker extends Checked {
  healthCheck("alive") { workerThreadIsActive() }
}
```

This creates and registers a health check named `com.example.ExampleWorker.alive` that is healthy when
`workerThreadIsActive()` returns `true`, and unhealthy with the default message `Health check failed` when it returns
`false`. You can override the unhealthy message as follows:

```scala
healthCheck("alive", unhealthyMessage = "Ouch!") { workerThreadIsActive() }
```

The code block may also return a `Try` (version 3.2.0 and later), a `Unit` or `Future` (version 3.5.0 and later), an
`Either` or a `com.codahale.metrics.health.HealthCheck.Result`. In these cases the `unhealthyMessage` parameter is
always ignored.

For more details see the scaladoc in [CheckedBuilder](/src/main/scala/nl/grons/metrics/scala/CheckedBuilder.scala).

### Warning for version 3.4.x and earlier

Due to the way the Scala compiler works you have to be careful with inline health checks that contain multiple
statements.

```scala
class ExampleWorker extends Checked {
  var counter: Int = 0
  healthCheck("alive") {
    counter += 1        // <-- executed only once!
    counter % 2 == 0    // <-- the actually health check, however this will return the same value always!
  }
}
```

In this example the compiler only takes the last expression in the block as the health check. As a solution either
upgrade to metrics-scala 3.5.0 or later, or change your code such that the last expression in the block contains all
your logic. For example:

```scala
class ExampleWorker extends Checked {
  var counter: Int = 0
  healthCheck("alive") {
    def check(): Boolean = {
      counter += 1
      counter % 2 == 0
    }
    check()
  }
}
```

The issue has been fixed in version 3.5.0. See also [issue 42](https://github.com/erikvanoosten/metrics-scala/issues/42).

## Health check names

Each health check has a unique name. In metrics-scala the name starts with a name derived from the *owner class*.
The owner class is the class that extends the `Checked` trait you defined earlier.

The health check name is build from:

* *Metric base name* By default this is set to the owner class name (e.g., `com.example.proj.auth.SessionStore`).
* *Name:* A short name describing the health check (e.g., `alive`).

Since 3.1.0 the *metric base name* can be overridden. For example by using `Checked` as follows:

```scala
import nl.grons.metrics.scala._

class Example extends Checked {
  override lazy val metricBaseName = MetricName("Overridden.Base.Name")
  ....
}
```

## About `Checked` and `Instrumented`

Although all Metrics-scala documentation refers to the `Checked` trait (as created in the text above), you are free to
chose the name, or in fact not to use it at all. It is also possible to directly extend `CheckedBuilder` and provide an
implementation of `healthCheckRegistry` in every class. Finally, since 3.5.5 you can instead also use the provided
`DefaultInstrumented`.

There is also opportunity to combine your `Instrumented` and `Checked` in a single trait (just like
`DefaultInstrumented` does):

```scala
import nl.grons.metrics.scala._

object YourApplication {
  /** The application wide metrics registry. */
  val metricRegistry = new com.codahale.metrics.MetricRegistry()
  /** The application wide health check registry. */
  val healthCheckRegistry = new com.codahale.metrics.health.HealthCheckRegistry()
}

trait Instrumented extends InstrumentedBuilder with CheckedBuilder {
  override lazy val metricBaseName = MetricName(getClass) // This line required with 3.1.0, optional since 3.1.1.
  val metricRegistry = YourApplication.metricRegistry
  val registry = YourApplication.healthCheckRegistry
}
```

This variant of `Instrumented` supports both building metrics and health checks.

Previous: [Manual](/docs/Manual.md) Up: [Manual](/docs/Manual.md) Next: [Instrumenting Futures](/docs/Futures.md)
