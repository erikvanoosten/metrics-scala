## Testing

(Available since metrics-scala 4.0.0.)

### Fresh registries

During unit testing it is common to many times instantiate the service class that is being tested. This is a problem
if your service defines a gauge or a health check (see note at bottom); you can not register multiple of those under
the same name.

Here is an example. The service class `Example` defines a gauge:

```scala
class Example(db: Database) extends nl.grons.metrics4.scala.DefaultInstrumented {
  // Define a gauge with a static name
  metrics.gauge("aGauge") { db.rowCount() }
}
```

The unit tests contain the following:

```scala
  import nl.grons.metrics4.scala.FreshRegistries
  val db = new Database
  val example1 = new Example(db)
  val example2 = new Example(db)   // BOOM!
```

This leads to something like:

```
java.lang.IllegalArgumentException: A metric named com.domain.app.Example.aGauge already exists
	at com.codahale.metrics.MetricRegistry.register(MetricRegistry.java:97)
	....
```

You can solve this problem by forcing each gauge/health check to have a different name. When the service class is
a singleton in production, this is a bit awkward.

The solution is to mixin `FreshRegistries`, but only in the unit tests:

```scala
  import nl.grons.metrics4.scala.FreshRegistries
  val db = new Database
  val example1 = new Example(db) with FreshRegistries
  val example2 = new Example(db) with FreshRegistries   // not a problem because a different metrics registry is used
```

Trait `FreshRegistries` makes sure that each instance of `Example` gets a new *fresh* metrics registry. It does the
same for the health check registry.

### Testing metrics and health checks

Another advantage of using `FreshRegistries` in unit tests is that it becomes possible to test that metric collection
is working as expected without any interference from other tests. For example:

```scala
class Example extends nl.grons.metrics4.scala.DefaultInstrumented {
  private val aCounter = metrics.counter("aCounter")

  def doSomething(): Unit = { aCounter += 1 } 
}
```

```scala
  val example = new Example with FreshRegistries
  example.doSomething()
  example.doSomething()
  assert(example.metrics.counter("aCounter").count == 2)    // assert counter was updated correctly
```

### Fresh registries for old style `Instrumented`

If you are not using `DefaultInstrumented`, what you mix in depends on the traits your service class (indirectly)
extends:

Extended trait        | Used for      | Fresh registry trait
--------------------- | ------------- | -----------------------------
`InstrumentedBuilder` | metrics       | `FreshMetricRegistry`
`CheckedBuilder`      | health checks | `FreshHealthCheckRegistry`
*both*                | *both*        | `FreshRegistries`


(*) Note: Before dropwizard-metrics 4.1 registering a health check under an existing name is a no-op. This means that
without the `FreshRegistries` your unit test will not fail, but you will not be able to test the behavior of your
health checks.


Previous: [Instrumenting Actors](Actors.md) Up: [Manual](Manual.md) Next: [Hdrhistogram](Hdrhistogram.md)
