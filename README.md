Metrics-Scala
=============

*Capturing JVM- and application-level metrics. So you know what's going on.*

This is the Scala API for [Coda Hale's Metrics](https://github.com/codahale/metrics) library.

Initially this project started out as a line for line copy of the Metrics-scala module, released for multiple
scala versions. Metrics dropped the scala module in version 3.0.0 and this project continued separately.

The 3.0.0 version is currently in development.

## Usage

Metrics-scala provides the ``Instrumented`` trait for Scala applications:

```scala
import nl.grons.metrics.scala.Instrumented
// import com.codahale.metrics.scala.Instrumented   <-- versions 2.x

class Example(db: Database) extends Instrumented {
  private val loading = metrics.timer("loading")

  def loadStuff(): Seq[Row] = loading.time {
    db.fetchRows()
  }
}
```

There are Scala wrappers for each metric type: `gauge`, `counter`, `histogram`, `meter` and `timer`.

More advanced usages are in the wiki (TODO!).

For more information on Metrics, please see [the documentation](http://metrics.codahale.com).

## Available versions

Please consult the table below to see which versions of metrics-scala are available for which scala versions.

Note that only the versions 2.1.4 and 2.1.5 support OSGI.

<table border="0" cellpadding="2" cellspacing="2">
  <tbody>
    <tr>
      <td valign="top"></td>
      <td valign="top"></td>
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
      <td colspan="1" rowspan="5" valign="top">Metrics<br>version</td>
      <td valign="top">2.1.2</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top"></td>
      <td valign="top"></td>
      <td valign="top"></td>
    </tr>
    <tr>
      <td valign="top">2.1.3</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top"></td>
      <td valign="top"></td>
      <td valign="top"></td>
    </tr>
    <tr>
      <td valign="top">2.1.4</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top"></td>
      <td valign="top"></td>
      <td valign="top"></td>
    </tr>
    <tr>
      <td valign="top">2.1.5</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top"></td>
      <td valign="top"></td>
      <td valign="top">✓</td>
    </tr>
    <tr>
      <td valign="top">2.2.0</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
      <td valign="top">✓</td>
    </tr>
  </tbody>
</table>

If you need another version mix please open an [issue](https://github.com/erikvanoosten/metrics-scala/issues), or
sent an email to the [metrics mailing list](http://groups.google.com/group/metrics-user).


## Get it

SBT:
```
libraryDependencies += "nl.grons" %% "metrics-scala" % "2.2.0"
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
    <version>2.2.0</version>
</dependency>
```

Note: For scala versions before 2.10, you need to use the full scala version; e.g. `metrics-scala_2.9.1-1`.

Note: If you depend on JMX: 2.2.0 has a small [bug](https://github.com/codahale/metrics/issues/318) that makes
it inconvenient to use JMX.


License
-------

Copyright (c) 2010-2012 Coda Hale, Yammer.com (before 3.0.0)

Copyright (c) 2013 Erik van Oosten (3.0.0 and later)

Published under Apache Software License 2.0, see LICENSE
