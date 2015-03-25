Metrics-Scala
=============

*Capturing JVM- and application-level metrics. So you know what's going on.*

This is the Scala API for [Dropwizard's Metrics](https://github.com/dropwizard/metrics) library.

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

For more detailed information see the [manual](docs/Manual.md). For more information on Metrics-core 3.x, please see the [documentation](https://dropwizard.github.io/metrics/3.1.0/).

See the [change log](CHANGELOG.md) for API changes compared to the 2.x versions.

### Features

* Easy creation of all metrics types.
* Easy creation of Health Checks.
* Almost invisible syntax for using timers (see example above).
* Scala specific methods on metrics (e.g. `+=` on counters).
* Derives proper metrics names for Scala objects and closures.
* Actor support.
* Future support.
* [Hdrhistogram](http://hdrhistogram.org/) support.

## Available versions (abbreviated)

This table shows the most relevant versions of metrics-scala. For the full list, including the Scala 2.9 versions, see [all available versions](docs/AvailableVersions.md).

<table border="0" cellpadding="2" cellspacing="2">
  <tbody>
    <tr>
      <td valign="top" rowspan="2">Metrics-<br>scala<br>version</td>
      <td valign="top" rowspan="2">Metrics-<br>core<br>version</td>
      <td valign="top" rowspan="2">Akka<br>version</td>
      <td colspan="2" rowspan="1" valign="top">Scala version</td>
      <td rowspan="2" valign="top">Hdr<br>version</td>
    </tr>
    <tr>
      <td valign="top">2.10</td>
      <td valign="top">2.11</td>
    </tr>
    <tr>
      <td valign="top">2.1.5</td>
      <td valign="top">2.1.5</td>
      <td valign="top"></td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">1.1.0</td>
    </tr>
    <tr>
      <td valign="top"><a href="CHANGELOG.md#v340-mar-2015">3.4.0</a></td>
      <td valign="top">3.1.0</td>
      <td valign="top"></td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">1.1.0</td>
    </tr>
    <tr>
      <td valign="top"><a href="CHANGELOG.md#v340-mar-2015">3.4.0_a2.1</a></td>
      <td valign="top">3.1.0</td>
      <td valign="top">2.1.4</td>
      <td valign="top">✓</td>
      <td valign="top"></td>
      <td valign="top">1.1.0</td>
    </tr>
    <tr>
      <td valign="top"><a href="CHANGELOG.md#v340-mar-2015">3.4.0_a2.2</a></td>
      <td valign="top">3.1.0</td>
      <td valign="top">2.2.5</td>
      <td valign="top">✓</td>
      <td valign="top"></td>
      <td valign="top">1.1.0</td>
    </tr>
    <tr>
      <td valign="top"><a href="CHANGELOG.md#v340-mar-2015">3.4.0_a2.3</a></td>
      <td valign="top">3.1.0</td>
      <td valign="top">2.3.9</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">1.1.0</td>
    </tr>
  </tbody>
</table>

If you need another version mix please open an [issue](https://github.com/erikvanoosten/metrics-scala/issues), or sent an email to the [metrics mailing list](http://groups.google.com/group/metrics-user).

Note: If Akka or hdrhistogram has a newer minor-version, you can use that instead of the version metrics-scala was build against.

## Download

SBT:
```
libraryDependencies += "nl.grons" %% "metrics-scala" % "3.3.0_a2.3"
```

Maven:
```
<properties>
    <scala.version>2.11.0</scala.version>
    <scala.dep.version>2.11</scala.dep.version>
</properties>
<dependency>
    <groupId>nl.grons</groupId>
    <artifactId>metrics-scala_${scala.dep.version}</artifactId>
    <version>3.3.0_a2.3</version>
</dependency>
```

To use hdrhistogram additional dependencies are needed. See the [hdrhistogram manual page](/docs/Hdrhistogram.md).

## Support

If you find a bug, please open an [issue](https://github.com/erikvanoosten/metrics-scala/issues), better yet: send a pull request.
For questions, please sent an email to the [metrics mailing list](http://groups.google.com/group/metrics-user).


### License

Copyright (c) 2010-2012 Coda Hale, Yammer.com (before 3.0.0)

Copyright (c) 2013-2015 Erik van Oosten (3.0.0 and later)

Published under Apache Software License 2.0, see [LICENSE](LICENSE)
