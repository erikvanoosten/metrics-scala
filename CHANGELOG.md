v3.0.1: July 2013
=================

* Upgrade to Metrics-core 3.0.1


v3.0.0: June 2013
=================

* As code is no longer maintained by Coda Hale, added and updated copyright statements to reflect this.
* Depends on Metrics-core 3.0.0.
* Ported tests from original to ScalaTest (thanks @scullxbones).
* Added documentation.

Although the metrics-scala API is mostly source compatible, there are breaking API changes
which are mostly caused by changes in the metrics-core library:

* All code moved to the `nl.grons.metrics.scala` package (changed at Coda Hale's request).
* All timers now measure in nanoseconds.
* All configuration for histograms, meters, and timers are gone. These are now configured in the reporter.
* The class `Instrumented` must now be created in your project by extending `InstrumentedBuilder`.
* Dropped method `clear` on `Histogram` and `Timer`.

Up to v2.2.0
============

* No code changes.


v2.1.2: Sep 10 2012
===================

* Initial copy from Coda Hale's Metrics project.

