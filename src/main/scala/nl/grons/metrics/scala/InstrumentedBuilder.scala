/*
 * Copyright (c) 2013-2014 Erik van Oosten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.grons.metrics.scala

import com.codahale.metrics.MetricRegistry

/**
 * The mixin trait for creating a class which is instrumented with metrics.
 *
 * Use it as follows:
 * {{{
 * object Application {
 *   // The application wide metrics registry.
 *   val metricRegistry = new com.codahale.metrics.MetricRegistry()
 * }
 * trait Instrumented extends InstrumentedBuilder {
 *   val metricRegistry = Application.metricRegistry
 * }
 *
 * class Example(db: Database) extends Instrumented {
 *   private[this] val loading = metrics.timer("loading")
 *
 *   def loadStuff(): Seq[Row] = loading.time {
 *     db.fetchRows()
 *   }
 * }
 * }}}
 *
 * If you'd like to derive the metric name from something other than the
 * instrumented class name, you can override `metricBaseName` to your own
 * [[nl.grons.metrics.scala.MetricName]] instance:
 * {{{
 * trait Instrumented extends InstrumentedBuilder {
 *   override lazy val metricBaseName = MetricName("some", "custom", "names")
 *   val metricRegistry = Application.metricRegistry
 * }
 * }}}
 */
trait InstrumentedBuilder {
  /** The base name for all metrics created from this builder. */
  lazy val metricBaseName = MetricName(getClass)

  private lazy val metricBuilder = new MetricBuilder(metricBaseName, metricRegistry)

  /**
   * The MetricBuilder that can be used for creating timers, counters, etc.
   */
  def metrics: MetricBuilder = metricBuilder

  /**
   * The MetricRegistry where created metrics are registered.
   */
  val metricRegistry: MetricRegistry
}
