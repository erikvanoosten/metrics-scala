/*
 * Copyright (c) 2013-2018 Erik van Oosten
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

package nl.grons.metrics5.scala

import io.dropwizard.metrics5.MetricRegistry.MetricSupplier
import io.dropwizard.metrics5.{Metric, MetricFilter, MetricName}

import scala.language.implicitConversions
import scala.collection.JavaConverters._

/**
  * Implicit conversions which allow usage of Dropwizard Metric api's with Scala types.
  *
  */
object Implicits {

  /**
    * A wrapper for [[MetricName]] that allows adding tags with a Scala [[Map]].
    */
  implicit class RichMetricName(val metricName: MetricName) extends AnyVal {
    /**
      * Add tags to a metric name and return the newly created MetricName.
      *
      * @param add Tags to add.
      * @return A newly created metric name with the specified tags associated with it.
      */
    def tagged(add: Map[String, String]): MetricName = metricName.tagged(add.asJava)
  }

  /**
    * Creates a [[MetricFilter]] from a regular Scala function that accepts a name and a metric and
    * returns a boolean: `(String, Metric) => Boolean`.
    *
    * Note: For more control (for example filter by tags), implement a [[MetricFilter]] directly.
    *
    * NOTE: no longer needed in Scala 2.12 and later.
    *
    * @param f the function to convert
    * @return a [[MetricFilter]]
    */
  implicit def functionToMetricFilter(f: (String, Metric) => Boolean): MetricFilter = new MetricFilter {
    override def matches(name: MetricName, metric: Metric): Boolean = f(name.getKey, metric)
  }

  /**
    * Creates a [[MetricSupplier]] from a regular Scala function that returns a [[Metric]]: `() => Metric`.
    *
    * NOTE: no longer needed in Scala 2.12 and later.
    *
    * @param f the function to convert
    * @tparam M the metric type
    * @return a [[MetricSupplier]]
    */
  implicit def functionToMetricSupplier[M <: Metric](f: () => M): MetricSupplier[M] = new MetricSupplier[M] {
    override def newMetric(): M = f()
  }

}
