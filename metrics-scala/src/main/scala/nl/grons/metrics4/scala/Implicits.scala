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

import com.codahale.metrics.MetricRegistry.MetricSupplier
import com.codahale.metrics.{Metric, MetricFilter}

import scala.language.implicitConversions

/**
  * Implicit conversions of Scala functions to Metric interfaces.
  *
  * NOTE: no longer needed in Scala 2.12 and later.
  */
object Implicits {
  /**
    * Creates a [[MetricFilter]] from a regular Scala function that accepts a name and a metric and
    * returns a boolean: `(String, Metric) => Boolean`.
    *
    * @param f the function to convert
    * @return a [[MetricFilter]]
    */
  implicit def functionToMetricFilter(f: (String, Metric) => Boolean): MetricFilter = new MetricFilter {
    override def matches(name: String, metric: Metric): Boolean = f(name, metric)
  }

  /**
    * Creates a [[MetricSupplier]] from a regular Scala function that returns a [[Metric]]: `() => Metric`.
    *
    * @param f the function to convert
    * @tparam M the metric type
    * @return a [[MetricSupplier]]
    */
  implicit def functionToMetricSupplier[M <: Metric](f: () => M): MetricSupplier[M] = new MetricSupplier[M] {
    override def newMetric(): M = f()
  }

}
