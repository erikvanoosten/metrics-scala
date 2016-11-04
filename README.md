Metrics-Scala
=============

*Capturing JVM- and application-level metrics. So you know what's going on.*

This is the Scala API for [Dropwizard's Metrics](https://github.com/dropwizard/metrics) library.

Initially this project started out as a line for line copy of the Metrics-scala module, released for multiple
scala versions. Metrics dropped the scala module in version 3.0.0 and this project continued separately
with the help of [@scullxbones](https://github.com/scullxbones).

We strive for long term stability, correctness, an easy to use API and full documentation (in that order).

### Contents

* Usage
* [Manual](/docs/Manual.md)
* [Manual (version 2.x)](/docs/Manual_2x.md)
* Features
* Available versions
* Download
* Support
* License
* [![Scaladocs](https://www.javadoc.io/badge/nl.grons/metrics-scala_2.11.svg?color=brightgreen&label=Scaladocs)](https://www.javadoc.io/doc/nl.grons/metrics-scala_2.11/3.5.5_a2.3)

### Usage

Metrics-scala provides an easy way to create _metrics_ and _health checks_ in Scala. Since version 3.5.5 creating
metrics and health checks is as easy as extending
[DefaultInstrumented](/src/main/scala/nl/grons/metrics/scala/DefaultInstrumented.scala) and using the `metrics` and
`healthCheck` builders:

```scala
class Example(db: Database) extends nl.grons.metrics.scala.DefaultInstrumented {
  // Define a health check
  healthCheck("alive") { workerThreadIsActive() }

  // Define a timer metric
  private[this] val loading = metrics.timer("loading")

  // Use timer metric
  def loadStuff(): Seq[Row] = loading.time {
    db.fetchRows()
  }
}
```

For more detailed information see the [manual](/docs/Manual.md). For more information on Metrics-core 3.x, please see
the [documentation](https://dropwizard.github.io/metrics/3.1.0/).

See also the [change log](CHANGELOG.md) for improvements and API changes.

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

This table shows the most relevant versions of metrics-scala. For the full list see
[all available versions](/docs/AvailableVersions.md).

<table border="0" cellpadding="2" cellspacing="2">
  <tbody>
    <tr>
      <td valign="top" rowspan="2">Metrics-<br>scala<br>version</td>
      <td valign="top" rowspan="2">Metrics-<br>core<br>version</td>
      <td valign="top" rowspan="2">Akka<br>version</td>
      <td colspan="3" rowspan="1" valign="top">Scala version</td>
      <td rowspan="2" valign="top">Hdr<br>version (*)</td>
    </tr>
    <tr>
      <td valign="top">2.10</td>
      <td valign="top">2.11</td>
      <td valign="top">2.12</td>
    </tr>
    <tr>
      <td valign="top">2.1.5</td>
      <td valign="top">2.1.5</td>
      <td valign="top"></td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top"></td>
      <td valign="top"></td>
    </tr>
    <tr>
      <td valign="top"><a href="CHANGELOG.md#v355-sep-2016">3.5.5</a></td>
      <td valign="top">3.1.2</td>
      <td valign="top"></td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">1.1.0/2.1.9</td>
    </tr>
    <tr>
      <td valign="top"><a href="CHANGELOG.md#v355-sep-2016">3.5.5_a2.2</a></td>
      <td valign="top">3.1.2</td>
      <td valign="top">2.2.5</td>
      <td valign="top">✓</td>
      <td valign="top"></td>
      <td valign="top"></td>
      <td valign="top">1.1.0/2.1.9</td>
    </tr>
    <tr>
      <td valign="top"><a href="CHANGELOG.md#v355-sep-2016">3.5.5_a2.3</a></td>
      <td valign="top">3.1.2</td>
      <td valign="top">2.3.15 / 2.4.x</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top"></td>
      <td valign="top">1.1.0/2.1.9</td>
    </tr>
    <tr>
      <td valign="top"><a href="CHANGELOG.md#v355-sep-2016">3.5.5_a2.4</a></td>
      <td valign="top">3.1.2</td>
      <td valign="top">2.4.11</td>
      <td valign="top"></td>
      <td valign="top"></td>
      <td valign="top">✓</td>
      <td valign="top">1.1.0/2.1.9</td>
    </tr>
  </tbody>
</table>

If you need another version mix please open an [issue](https://github.com/erikvanoosten/metrics-scala/issues), or sent
an email to the [metrics mailing list](http://groups.google.com/group/metrics-user).

Note: If Akka has a newer minor-version, you can use that instead of the version metrics-scala was build against. (In
addition, if you want to use Akka 2.4 with scala 2.10 and 2.11, you use the build for Akka 2.3 as Akka 2.4 is binary
compatible with Akka 2.3.)

(*) Hdr is an optional dependency. The first number is the version of
`"org.mpierce.metrics.reservoir" % "hdrhistogram-metrics-reservoir"`, the second the version of
`"org.hdrhistogram" % "HdrHistogram"`. See also [hdrhistogram manual page](/docs/Hdrhistogram.md).

## Download

SBT:
```
libraryDependencies += "nl.grons" %% "metrics-scala" % "3.5.5_a2.4"
```

Maven:
```
<properties>
    <scala.version>2.12.0</scala.version>
    <scala.dep.version>2.12</scala.dep.version>
</properties>
<dependency>
    <groupId>nl.grons</groupId>
    <artifactId>metrics-scala_${scala.dep.version}</artifactId>
    <version>3.5.5_a2.4</version>
</dependency>
```

To use hdrhistogram additional dependencies are needed. See the [hdrhistogram manual page](/docs/Hdrhistogram.md).

## Support

If you find a bug, please open an [issue](https://github.com/erikvanoosten/metrics-scala/issues), better yet: send a
pull request. For questions, please sent an email to the
[metrics mailing list](http://groups.google.com/group/metrics-user).

### License

Copyright (c) 2010-2012 Coda Hale, Yammer.com (before 3.0.0)

Copyright (c) 2013-2016 Erik van Oosten (3.0.0 and later)

Published under Apache Software License 2.0, see [LICENSE](LICENSE)
