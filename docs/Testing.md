## Testing

(Available since metrics-scala 4.0.0.)

### Fresh registries

During unit testing it is common to many times instantiate the service class that is being tested. This is a problem
if your service has a gauge; you can not register multiple gauges under the same name.

Example. The service class `Example`

```scala
class Example(db: Database) extends nl.grons.metrics.scala.DefaultInstrumented {
  // Define a gauge with a static name
  metrics.gauge("aGauge") { db.rowCount() }
}
```

and the following unit test fragment:

```scala
  import nl.grons.metrics.scala.FreshRegistries
  val db = new Database
  val example1 = new Example(db)
  val example2 = new Example(db)   // BOOM!
```

leads to something like:

```
java.lang.IllegalArgumentException: A metric named com.domain.app.Example.aGauge already exists
	at com.codahale.metrics.MetricRegistry.register(MetricRegistry.java:97)
	....
```

You can solve this problem by forcing each gauge to have a different name. This is often awkward as in production
code the service class is a singleton.

The solution is to mixin `FreshRegistries`, but only in the unit tests:

```scala
  import nl.grons.metrics.scala.FreshRegistries
  val db = new Database
  val example1 = new Example(db) with FreshRegistries
  val example2 = new Example(db) with FreshRegistries   // not a problem because a different metrics registry is used
```

Trait `FreshRegistries` makes sure that each instance of `Example` gets a new *fresh* metrics registry. It does the
same for the health check registry.

*Side note* The problem with gauges should probably also be a problem with health checks. However, currently health
check registries simply ignore duplicate registrations even though maybe they should not. This may change when
https://github.com/dropwizard/metrics/issues/1245 is accepted.

### Testing metrics

Another advantage of using `FreshRegistries` in unit tests is that it becomes possible to test that metric collection
is working as expected without any interference from other tests. For example:

```scala
class Example extends nl.grons.metrics.scala.DefaultInstrumented {
  val aCounter = metrics.counter("aCounter")

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


Previous: [Instrumenting Actors](Actors.md) Up: [Manual](Manual.md) Next: [Hdrhistogram](Hdrhistogram.md)
