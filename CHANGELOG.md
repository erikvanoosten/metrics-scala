v4.0.4: Jan 2019
================

* No code changes.
* Updates: scala 2.12.7 -> 2.12.8, dropwizard metrics: 4.0.3 -> 4.0.5, HdrHistogram: 2.1.10 -> 2.1.11,
  Akka: 2.5.18 -> 2.5.19.
* Added scala 2.13.0-M5. Note: this version is not supported.

v4.0.3: Nov 2018
================

* Fixed an (unlikely to happen) off-by-one error introduced in v4.0.2.
* Deprecated the `scope` parameter in the metric builder so that it can be removed in v5.0.0.

v3.5.10: Nov 2018
=================

* Deprecated the `scope` parameter in the metric builder so that it can be removed in v5.0.0.

v4.0.2: Nov 2018
================

* Drastic performance improvements for metric name generation. This is especially important for dynamically generated
  metric names. This improvement was authored by Filip Ochnik (@filipochnik). Thanks Filip! (#117)
* Compiles against Scala 2.13 betas, metrics-scala is now part of the Scala community build.
* Lots of dependency updates (also thanks to Scala Steward): Dropwizard metrics 4.0.1 -> 4.0.3, Akka 2.5.8 -> 2.5.18,
  HdrHistogram 2.1.9 -> 2.1.10, Scala 2.12.4 -> 2.12.7, Scalatest 3.0.4 -> 3.0.5, Mockito 1.10.19 -> 2.23.0. 
* Upgraded sbt from 1.0.4 to 1.2.6.

v4.0.1: Jan 2018
================

* All code moved to the package `nl.grons.metrics4.scala` to prevent clashes with version 3.x. Except for the package
  name change, `v4.0.1` is fully source compatible with `v3.5.9`.
* Added trait `FreshRegistries`, `FreshMetricRegistry` and `FreshHealthCheckRegistry` to enable testing that metrics
  are collected. Furthermore it eases unit testing of components that define gauges or health checks
  (see documentation).
* Using dropwizard-metrics 4.0.1, only targeting Java 8 and later.
* Bumped Scala and Akka to latest minor versions. 
* Dropped support for Scala 2.10 and Akka 2.3.
* Removed deprecations. (Trait `FutureMetrics` and method `Meter.exceptionMarkerPartialFunction`.)
* Akka support moved to a separate library: `"nl.grons" %% "metrics4-akka_a24"` and `"nl.grons" %% "metrics4-akka_a25"`
* Hdr support moved to a separate library: `"nl.grons" %% "metrics4-scala-hdr"`
* Upgraded sbt from 0.13 to 1.0. Complete cross build can not be done with a single sbt invocation.

v4.0.0: Jan 2018
================

v4.0.0 was released but should not be used as it is not compatible with v3.x.

v3.5.9: Jun 2017
================

* Further performance improvement for creating dynamically created HDR metrics, thanks to the very attentive
  @OlegIlyenko (and again @agourlay) (#92)

v3.5.8: Jun 2017
================

* Regression from 3.5.7: fixed a race condition in creating dynamically created HDR metrics, thanks go to @agourlay (#91) 

v3.5.7: Jun 2017
================

* A performance optimization for dynamically created HDR metrics, a fine contribution by @truemped and @otrosien (#90) 
* Version bumps: Metrics-core 3.2.0 -> 3.2.2, Scala 2.12.1 -> 2.12.2, Scala 2.11.8 -> 2.11.11, Akka 2.4.17 -> 2.4.18

v3.5.6: Feb 2017
================

* Version bumps: Metrics-core 3.1.2 -> 3.2.0, Scala 2.12.0 -> 2.12.1, Akka 2.3.15 -> 2.3.16 and 2.4.14 -> 2.4.17.
* Dropped Akka 2.2 support.

v3.5.5: Sep 2016
================

* Measure also when the value-by-name parameter of `timeFuture` throws (#78) by @Slakah. Thanks JCollier!
* Introduces `DefaultInstrumented`, a great convenience introduced by @jklukas (#80). Good idea Jeff!
* Bumped optional HdrHistogram dependency from 2.1.6 to 2.1.9.
* (2016-10-06) Released for scala 2.12.0-RC1 and akka 2.4.11 (no longer supported).
  This release was not possible without the work of @takezoe (#83). Thanks Naoki!
* (2016-10-26) Released for scala 2.12.0-RC2 and akka 2.4.11 (no longer supported).
* (2016-11-04) Released for scala 2.12.0 and akka 2.4.12.

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

