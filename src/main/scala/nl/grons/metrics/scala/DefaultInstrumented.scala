/*
 * Copyright (c) 2016 Erik van Oosten
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

/**
  * A mixin trait for creating a class that publishes metrics to the "default" registry.
  *
  * This is a useful default for Dropwizard 1.0.0+ applications where
  * the Dropwizard environment publishes its built-in metrics registry as "default".
  */

trait DefaultInstrumented extends InstrumentedBuilder {
  if (!SharedMetricRegistries.names().contains("default")) {
    throw new IllegalStateException("No registry named \"default\" found in SharedMetricRegistries")
  }
  val metricRegistry = SharedMetricRegistries.getOrCreate("default")
}
