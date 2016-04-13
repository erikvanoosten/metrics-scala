v3.5.4: Apr 2016
================

* Removed double class from metric name (#75), reported by Per Rovegård (@provegard). Thanks Per!
* Improved metric names for metrics in nested classes and in anonymous sub-classes.
* _Deprecated_ class `FutureMetrics`.
* Added `Timer.timeFuture`. This fixes #68 for real, thanks to  @maciej for the thought seed.
* Akka 2.3 bumped to 2.3.15.

v3.5.3: Mar 2016
================

* Introduced `ActorInstrumentedLifeCycle` to automatically unregister gauges in actors (#69). It took quite some
  time to find a simple solution. Thanks go to @mrmeyers99, @ymeymann and @scullxbones for ideas and example code.
* Akka 2.3 bumped to 2.3.14, Scala 2.10 bumped to 2.10.6, Scala 2.11 bumped to 2.11.8.

v3.5.2: Sep 2015
================

* Added implicits to write MetricFilters as a scala function, idea and code from @maciej (#62).
* Removed Akka 2.1 support.
* Akka 2.3 bumped to 2.3.13, Scala 2.11 bumped to 2.11.7.

v3.5.1: May 2015
================

* Added method `countConcurrency` to counters, idea from @fedeoasi, pull request by @gshakhn (#60).
* For Akka 2.3 build: build against Akka 2.3.11.

v3.5.0: Apr 2015
================

* This release is not binary, nor source compatible with 3.4.x.
  The incompatibility is restricted to health-checks. Previously a code block written as second parameter
  for method `healthCheck` would be evaluated at the call-side, using only the last expression in the
  block as pass-by-name parameter. Now, the entire block is passed as a call-by-value parameter. The latter
  is more what you would expect, so you probably won't notice any incompatibility.
* Health-checks now support `Unit` and `Future` checkers. Based on a pull request from Scala virtuoso @davidhoyt (#59).
  (Also, this solves #42 for real.)
* Build against io.dropwizard.metrics 3.1.2.

v3.4.0: Mar 2015
================

* Added support for [hdrhistogram](http://hdrhistogram.org/).
* For Akka 2.2 build: build against Akka 2.2.5.
* For Akka 2.3 build: build against Akka 2.3.9.

v3.3.0: Sep 2014
================

* Build against io.dropwizard.metrics 3.1.0.
* For Akka 2.3 build: build against Akka 2.3.6.

v3.2.1: Aug 2014
================

* For future timing, invoke argument only once, closes #50. Bug report and solution from @JeffBellegarde.
* Added support for cached gauge, a nice pull request by @pasieronen (#52).
* For Akka 2.3 build: build against Akka 2.3.5.

v3.2.0: May 2014
================

* This release is not binary compatible with 3.1.x.
* Properly support multi-line health-checks, closes #42. An incredibly subtle bug reported by @joster-jambit, with a nifty solution from @som-snytt.
  (Update: the problem has been fully solved in version 3.5.0.)
* Added support for `Try` in Health-checks, an idea of our own @scullxbones.
* `Future`s are now supported in the build without Akka (solves #44 by @yatskevich).
* Allow measurements of `Future`s that were created elsewhere (#45).

v3.1.1.1: April 2014
==================

* Only released for Scala 2.11 and Akka 2.3.
* This release fixed a test dependency which was accidentally declared as regular dependency (#40) thanks for the report @lvicentesanchez.

v3.1.1: April 2014
==================

* Solved problem with conflicting inherited members in the version 3.1.0 (#37) reported by @dvallejo, thanks!

v3.1.0: April 2014
==================

* Build against no Akka (closes #32, #33, #21), Akka 2.1.4, 2.2.4 and 2.3.2.
* Now also build for Scala 2.11 (closes #34).
* Entire cross-build is now done with a script (#30, #31), based on research from @scullxbones.
* ".package" in metrics name (closes #19), thanks @akalinovskiy!
* Allow non-Class-based metric naming, #35, a great idea from @arosien

v3.0.5: March 2014
==================

* Build against Metrics-core 3.0.2.
* Build against Akka 2.1.4, 2.2.4 and 2.3.0.
* Small documentations correction (thanks @oschrenk!)
* Fixed description in pom.

v3.0.4: December 2013
=====================

* Same release build against Akka 2.2.0 instead of `[2.2,)`.

v3.0.3_a2.1.0: September 2013
=============================

* Same release build against Akka 2.1.0 instead of 2.2.0.

v3.0.3: August 2013
===================

* Added support for health checks (thanks @scullxbones), [#17](https://github.com/erikvanoosten/metrics-scala/issue/17). The module metrics-healthchecks is now a required dependency.

v3.0.2: August 2013
===================

* added back inc/dec on Counter (thanks @alexy), [#14](https://github.com/erikvanoosten/metrics-scala/issue/14)
* Renamed `Timer.time` for partial functions to prevent type annotations (thanks @scullxbones), [#13](https://github.com/erikvanoosten/metrics-scala/issue/13), [#15](https://github.com/erikvanoosten/metrics-scala/issue/15).
* Renamed `Meter.exceptionMarkerPartialFunction` to `Meter.exceptionMarkerPF` to be consistent with `Timer`.

v3.0.1: August 2013
===================

* Upgrade to Metrics-core 3.0.1, [#10](https://github.com/erikvanoosten/metrics-scala/issue/10).
* Added support for partial functions (thanks @scullxbones).
* Added support for actors and futures (scala 2.10 only) for timing and metering exceptions (thanks @scullxbones), [#8](https://github.com/erikvanoosten/metrics-scala/issue/8).
* Removes '$' from metrics names, [#11](https://github.com/erikvanoosten/metrics-scala/issue/11).


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

