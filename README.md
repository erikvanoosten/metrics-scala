Metrics-Scala
=============

*Capturing JVM- and application-level metrics. So you know what's going on.*

This is the Scala API for [Coda Hale's Metrics](https://github.com/codahale/metrics) library.

Initially this project started out as a line for line copy of the Metrics-scala module, released for multiple
scala versions. Metrics dropped the scala module in version 3.0.0 and this project continued separately
with the help of [@scullxbones](https://github.com/scullxbones).

### Contents

* Usage (version 3.x)
* Usage (version 2.x)
* Features
* Available versions
* Download
* License

### Usage (version 3.x)

In Metrics 3 you have to specify an application wide `MetricRegistry`. Create an
`Instrumented` trait that refers to that registry and that extends the `InstrumentedBuilder`
trait.

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

There are Scala wrappers for each metric type: `gauge`, `counter`, `histogram`, `meter` and `timer`.

For more information on Metrics 3.x, please see the [documentation](http://metrics.codahale.com).

See the [change log](CHANGELOG.md) for API changes compared to the 2.x versions.

### Usage (version 2.x)

Metrics-scala provides the ``Instrumented`` trait for Scala applications. This
trait gives you the metrics builder `metrics`.

```scala
import com.yammer.metrics.scala.Instrumented

class Example(db: Database) extends Instrumented {
  private[this] val loading = metrics.timer("loading")

  def loadStuff(): Seq[Row] = loading.time {
    db.fetchRows()
  }
}
```

There are Scala wrappers for each metric type: `gauge`, `counter`, `histogram`, `meter` and `timer`.

For more information on Metrics 2.x, please see the
[documentation in the Way Back Machine](http://web.archive.org/web/20120925003800/http://metrics.codahale.com/manual/core/)
or read it directly from [Metrics 2.2.0 git branch](https://github.com/codahale/metrics/tree/v2.2.0/docs/source/manual).

### Features

* Easy creation of all metrics types.
* Almost invisible syntax for using timers (see example below).
* Scala specific methods on metrics (e.g. `+=` on counters).

Planned:

* Health check support.
* Actor support.
* Future support.

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
      <td valign="top">2.10.0-RC1</td>
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
      <td valign="top"></td>
    </tr>
    <tr>
      <td valign="top">2.1.5</td>
      <td valign="top">same</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top"></td>
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
      <td valign="top">✓</td>
    </tr>
    <tr>
      <td valign="top"><a href="https://github.com/erikvanoosten/metrics-scala/releases/tag/version-3.0.0">3.0.0</a></td>
      <td valign="top">same</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
    </tr>
    <tr>
      <td valign="top"><a href="https://github.com/erikvanoosten/metrics-scala/releases/tag/version-3.0.1">3.0.1</a></td>
      <td valign="top">same</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
    </tr>
    <tr>
      <td valign="top"><a href="https://github.com/erikvanoosten/metrics-scala/releases/tag/version-3.0.2">3.0.2</a></td>
      <td valign="top">3.0.1</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
    </tr>
  </tbody>
</table>

If you need another version mix please open an [issue](https://github.com/erikvanoosten/metrics-scala/issues), or sent an email to the [metrics mailing list](http://groups.google.com/group/metrics-user).


## Download

SBT:
```
libraryDependencies += "nl.grons" %% "metrics-scala" % "3.0.2"
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
    <version>3.0.2</version>
</dependency>
```

Note: For scala versions before 2.10, you need to use the full scala version in the artifact name; e.g. `metrics-scala_2.9.1-1`.

Note: If you depend on JMX: 2.2.0 has a small [bug](https://github.com/codahale/metrics/issues/318) that makes it inconvenient to use JMX.


### License

Copyright (c) 2010-2012 Coda Hale, Yammer.com (before 3.0.0)

Copyright (c) 2013 Erik van Oosten (3.0.0 and later)

Published under Apache Software License 2.0, see [LICENSE](LICENSE)
