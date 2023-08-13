/*
 * Copyright (c) 2013-2023 Erik van Oosten
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

package nl.grons.metrics4.scala

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
 * As an alternative to your own `Instrumented` as above, it is possible to use [[DefaultInstrumented]] instead.
 *
 * By default metric names are prefixed with the name of the current class. You can override this metric base name.
 * For example:
 * {{{
 * class Example(db: Database) extends Instrumented {
 *   override lazy val metricBaseName = MetricName("Overridden.Base.Name")
 *   private[this] val loading = metrics.timer("loading")
 *
 *   def loadStuff(): Seq[Row] = loading.time {
 *     db.fetchRows()
 *   }
 * }
 * }}}
 *
 * If you want to use hdrhistograms, you can override the metric builder as follows:
 * {{{
 * trait Instrumented extends InstrumentedBuilder {
 *   override lazy protected val metricBuilder = new HdrMetricBuilder(metricBaseName, metricRegistry, false)
 *   val metricRegistry = Application.metricRegistry
 * }
 * }}}
 *
 * See the [[https://github.com/erikvanoosten/metrics-scala/blob/master/docs/Hdrhistogram.md the manual]]
 * for more instructions on using hdrhistogram.
 */
trait InstrumentedBuilder extends BaseBuilder {
  protected lazy val metricBuilder = new MetricBuilder(metricBaseName, metricRegistry)

  /**
   * The MetricBuilder that can be used for creating timers, counters, etc.
   */
  def metrics: MetricBuilder = metricBuilder

  /**
   * The MetricRegistry where created metrics are registered.
   */
  def metricRegistry: MetricRegistry
}
