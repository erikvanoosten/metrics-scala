/*
 * Copyright (c) 2013-2022 Erik van Oosten
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
import com.codahale.metrics.health.HealthCheckRegistry

/**
  * A mixin trait to get a fresh empty metric and health check registry every time.
  *
  * Often a singleton service class defines a gauge with a static name. However, during testing multiple instances
  * are needed. Unfortunately the metrics registry doesn't allow registering a gauge under the same name twice.
  * This is not a problem when a new registry is used for each instance.
  *
  * The same is the case for health checks (since dropwizard-metrics 4.1).
  *
  * *Example*
  *
  * With the following gauge in class `Example`:
  *
  * {{{
  * class Example(db: Database) extends nl.grons.metrics4.scala.DefaultInstrumented {
  *   // Define a gauge with a static name
  *   metrics.gauge("aGauge") { db.rowCount() }
  * }
  * }}}
  *
  * This trait can be mixed in with any instance of `Example`:
  *
  * {{{
  *   val example = new Example(db) with FreshRegistries
  * }}}
  *
  * See also [[FreshMetricRegistry]] and [[FreshHealthCheckRegistry]] in case your class only extends
  * [[InstrumentedBuilder]] or [[CheckedBuilder]] respectively.
  */
trait FreshRegistries { self: InstrumentedBuilder with CheckedBuilder =>
  override lazy val metricRegistry: MetricRegistry = new MetricRegistry
  override lazy val registry: HealthCheckRegistry = new HealthCheckRegistry
}

/**
  * A mixin trait to get a fresh empty metric registry every time.
  *
  * See [[FreshRegistries]] for more information.
  */
trait FreshMetricRegistry { self: InstrumentedBuilder =>
  override lazy val metricRegistry: MetricRegistry = new MetricRegistry
}

/**
  * A mixin trait to get a fresh empty health check registry every time.
  *
  * See [[FreshRegistries]] for more information.
  */
trait FreshHealthCheckRegistry { self: CheckedBuilder =>
  override lazy val registry: HealthCheckRegistry = new HealthCheckRegistry
}
