Metrics-Scala
=============

*Capturing JVM- and application-level metrics. So you know what's going on.*

This is the Scala API for [Coda Hale's Metrics](https://github.com/codahale/metrics) library.

Initially this project started out as a line for line copy of the Metrics-scala module, released for multiple
scala versions. Metrics dropped the scala module in version 3.0.0 and this project continued separately
with the help of [@scullxbones](https://github.com/scullxbones).

### Contents

* Usage
* [Manual](docs/Manual.md)
* [Manual (version 2.x)](docs/Manual_2x.md)
* Features
* Available versions
* Download
* Support
* License

### Usage

Metrics-scala provides an easy way to create metrics and health checks in Scala. Metrics-core requires an application wide `MetricRegistry`. Create an **`Instrumented`** trait that refers to that registry and extends the `InstrumentedBuilder` trait.

```scala
object YourApplication {
  /** The application wide metrics registry. */
  val metricRegistry = new com.codahale.metrics.MetricRegistry()
}
trait Instrumented extends nl.grons.metrics.scala.InstrumentedBuilder {
  val metricRegistry = YourApplication.metricRegistry
}
```

Now you can create metrics by using the `metrics` metrics builder.

```scala
class Example(db: Database) extends Instrumented {
  private[this] val loading = metrics.timer("loading")

  def loadStuff(): Seq[Row] = loading.time {
    db.fetchRows()
  }
}
```

For more detailed information see the [manual](docs/Manual.md). For more information on Metrics-core 3.x, please see the [documentation](http://metrics.codahale.com).

See the [change log](CHANGELOG.md) for API changes compared to the 2.x versions.

### Features

* Easy creation of all metrics types.
* Easy creation of Health Checks.
* Almost invisible syntax for using timers (see example above).
* Scala specific methods on metrics (e.g. `+=` on counters).
* Derives proper metrics names for Scala objects and closures.
* Actor support (Scala 2.10 only).
* Future support (Scala 2.10 only).

## Available versions

Please consult the table below to see which versions of metrics-scala are available for which scala versions.

Note that only the versions 2.1.4 and 2.1.5 support OSGI.

<table border="0" cellpadding="2" cellspacing="2">
  <tbody>
    <tr>
      <td valign="top">Metrics-<br>scala<br>version</td>
      <td valign="top">Metrics-<br>core<br>version</td>
      <td colspan="6" rowspan="1" valign="top">Scala version</td>
    </tr>
    <tr>
      <td valign="top"></td>
      <td valign="top"></td>
      <td valign="top">2.9.1</td>
      <td valign="top">2.9.1-1</td>
      <td valign="top">2.9.2</td>
      <td valign="top">2.9.3</td>
      <td valign="top">2.10.x</td>
    </tr>
    <tr>
      <td valign="top">2.1.2</td>
      <td valign="top">same</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top"></td>
      <td valign="top"></td>
    </tr>
    <tr>
      <td valign="top">2.1.3</td>
      <td valign="top">same</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top"></td>
      <td valign="top"></td>
    </tr>
    <tr>
      <td valign="top">2.1.4</td>
      <td valign="top">same</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top"></td>
      <td valign="top"></td>
    </tr>
    <tr>
      <td valign="top">2.1.5</td>
      <td valign="top">same</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top"></td>
      <td valign="top">✓</td>
    </tr>
    <tr>
      <td valign="top">2.2.0</td>
      <td valign="top">same</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
    </tr>
    <tr>
      <td valign="top"><a href="CHANGELOG.md#v300-june-2013">3.0.0</a></td>
      <td valign="top">same</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
    </tr>
    <tr>
      <td valign="top"><a href="CHANGELOG.md#v301-august-2013">3.0.1</a></td>
      <td valign="top">same</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
    </tr>
    <tr>
      <td valign="top"><a href="CHANGELOG.md#v302-august-2013">3.0.2</a></td>
      <td valign="top">3.0.1</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
    </tr>
    <tr>
      <td valign="top"><a href="CHANGELOG.md#v303-august-2013">3.0.3</a> (Akka 2.2.0)</td>
      <td valign="top">3.0.1</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
    </tr>
    <tr>
      <td valign="top"><a href="CHANGELOG.md#v303_a210-september-2013">3.0.3_a2.1.0</a> (Akka 2.1.0)</td>
      <td valign="top">3.0.1</td>
      <td valign="top"></td>
      <td valign="top"></td>
      <td valign="top"></td>
      <td valign="top"></td>
      <td valign="top">✓</td>
    </tr>
  </tbody>
</table>

If you need another version mix please open an [issue](https://github.com/erikvanoosten/metrics-scala/issues), or sent an email to the [metrics mailing list](http://groups.google.com/group/metrics-user).


## Download

SBT:
```
libraryDependencies += "nl.grons" %% "metrics-scala" % "3.0.3"
```

Note: If you add this dependency to a Play! framework project, you might need to override the akka dependencies of metrics-scala as it would introduce Akka 2.3. To do so add the following to the dependency overrides:

```
dependencyOverrides += "com.typesafe.akka" %% "akka-actor" % "2.2.0"
```

Maven:
```
<properties>
    <scala.version>2.10.0</scala.version>
    <scala.dep.version>2.10</scala.dep.version>
</properties>
<dependency>
    <groupId>nl.grons</groupId>
    <artifactId>metrics-scala_${scala.dep.version}</artifactId>
    <version>3.0.3</version>
</dependency>
```

Note: For scala versions before 2.10, you need to use the full scala version in the artifact name; e.g. `metrics-scala_2.9.1-1`.

Note: If you depend on JMX: 2.2.0 has a small [bug](https://github.com/codahale/metrics/issues/318) that makes it inconvenient to use JMX.

## Support

If you found a bug, please open an [issue](https://github.com/erikvanoosten/metrics-scala/issues), better yet: send a pull request.
For questions, please sent an email to the [metrics mailing list](http://groups.google.com/group/metrics-user).


### License

Copyright (c) 2010-2012 Coda Hale, Yammer.com (before 3.0.0)

Copyright (c) 2013 Erik van Oosten (3.0.0 and later)

Published under Apache Software License 2.0, see [LICENSE](LICENSE)
