Metrics-Scala
=============

*Capturing JVM- and application-level metrics. So you know what's going on.*

For more information, please see [the documentation](http://metrics.codahale.com).

This is a line for line copy of the Metrics-scala module of
[Metrics](https://github.com/codahale/metrics) released for multiple
scala versions.

Tests are stripped because they depend on a library that is for scala 2.9.1 only.

## Available versions

SBT:
```
libraryDependencies += "nl.grons" %% "metrics-scala" % "2.1.2"
```

Maven:
```
<properties>
    <scala.version>2.9.2</scala.version>
</properties>
<dependency>
    <groupId>nl.grons</groupId>
    <artifactId>metrics-scala_${scala.version}</artifactId>
    <version>2.1.2</version>
</dependency>
```

This version of Metrics-scala is build for scala 2.9.1, 2.9.1-1 and 2.9.2.
It will be build for 2.10 as it becomes available.


License
-------

Copyright (c) 2010-2012 Coda Hale, Yammer.com

Published under Apache Software License 2.0, see LICENSE
