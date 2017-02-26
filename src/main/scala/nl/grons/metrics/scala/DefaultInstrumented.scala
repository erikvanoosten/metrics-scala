/*
 * Copyright (c) 2016-2017 Erik van Oosten
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

import com.codahale.metrics.SharedMetricRegistries
import com.codahale.metrics.health.SharedHealthCheckRegistries

/**
  * A mixin trait for creating a class that publishes metrics and health checks to the "default" registries.
  *
  * This follows the Dropwizard 1.0.0+ application convention of storing the metric registry to
  * [[SharedMetricRegistries]] under the name `"default"`. This was extended with storing the health check registry to
  * [[SharedHealthCheckRegistries]] under the same name.
  *
  * After mixing in this trait, metrics and health checks can be defined. For example:
  * {{{
  * class Example(db: Database) extends DefaultInstrumented {
  *   // Define a health check:
  *   healthCheck("alive") { workerThreadIsActive() }
  *
  *   // Define a timer metric:
  *   private[this] val loading = metrics.timer("loading")
  *
  *   // Use the timer metric:
  *   def loadStuff(): Seq[Row] = loading.time {
  *     db.fetchRows()
  *   }
  * }
  * }}}
  *
  * See [[InstrumentedBuilder]] for instruction on overriding the metric base name or using hdrhistograms.
  * See [[CheckedBuilder]] for instructions on overriding the timeout for [[scala.concurrent.Future]] executions.
  */
trait DefaultInstrumented extends InstrumentedBuilder with CheckedBuilder {
  val metricRegistry = SharedMetricRegistries.getOrCreate("default")
  val registry = SharedHealthCheckRegistries.getOrCreate("default")
}
