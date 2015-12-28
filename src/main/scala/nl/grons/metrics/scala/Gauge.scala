/*
 * Copyright (c) 2013-2015 Erik van Oosten
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

import com.codahale.metrics.{Gauge => DropwizardGauge, MetricFilter, Metric}

object Gauge {
  def apply[A](f: => A) = new Gauge[A](new DropwizardGauge[A] {
    def getValue = f
  })
}

/**
 * A Scala facade class for [[DropwizardGauge]].
 */
class Gauge[T](metric: DropwizardGauge[T]) {

  /**
   * The current value.
   */
  def value: T = metric.getValue

}

import collection.JavaConverters._

/**
  * A gauge cleanup helper trait for [[DropwizardGauge]]
  */
trait GaugeCleanup { self: InstrumentedBuilder =>

  /**
    * Removes matching gauges from registry
    *
    * @param namePrefix is used to apply a leading string match to gauges to remove
    */
  def cleanupByPrefix(namePrefix: String) {
    metricRegistry.getGauges(new MetricFilter {
      override def matches(name: String, metric: Metric): Boolean = name.startsWith(namePrefix)
    }).keySet().asScala.foreach(metricRegistry.remove)
  }
}