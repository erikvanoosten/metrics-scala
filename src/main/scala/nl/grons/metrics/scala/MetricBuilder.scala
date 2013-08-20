/*
 * Copyright (c) 2013-2013 Erik van Oosten
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
import com.codahale.metrics.{Gauge => CHGauge}

/**
 * Builds and registering metrics.
 */
class MetricBuilder(val owner: Class[_], val registry: MetricRegistry) {

  /**
   * Registers a new gauge metric.
   *
   * @param name  the name of the gauge
   * @param scope the scope of the gauge or null for no scope
   */
  def gauge[A](name: String, scope: String = null)(f: => A): Gauge[A] =
    new Gauge[A](registry.register(metricName(name, scope), new CHGauge[A] { def getValue: A = f }))

  /**
   * Creates a new counter metric.
   *
   * @param name  the name of the counter
   * @param scope the scope of the counter or null for no scope
   */
  def counter(name: String, scope: String = null): Counter =
    new Counter(registry.counter(metricName(name, scope)))

  /**
   * Creates a new histogram metrics.
   *
   * @param name   the name of the histogram
   * @param scope  the scope of the histogram or null for no scope
   */
  def histogram(name: String, scope: String = null): Histogram =
    new Histogram(registry.histogram(metricName(name, scope)))

  /**
   * Creates a new meter metric.
   *
   * @param name the name of the meter
   * @param scope the scope of the meter or null for no scope
   */
  def meter(name: String, scope: String = null): Meter =
    new Meter(registry.meter(metricName(name, scope)))

  /**
   * Creates a new timer metric.
   *
   * @param name the name of the timer
   * @param scope the scope of the timer or null for no scope
   */
  def timer(name: String, scope: String = null): Timer =
    new Timer(registry.timer(metricName(name, scope)))

  private[this] def metricName(name: String, scope: String = null): String =
    MetricBuilder.metricName(owner, Seq(name, scope))

}

object MetricBuilder {
  /**
   * Create a metrics name.
   * Unlike [[com.codahale.metrics.MetricRegistry.name()]] this version supports Scala classes
   * such as objects and closures.
   *
   * @param owner the owning class
   * @param names the parts of the metric name, any `null`s are ignored
   * @return owner's class, name and names concatenated by periods
   */
  def metricName(owner: Class[_], names: Seq[String]): String = {
    // Example weird class name: TestContext$$anonfun$2$$anonfun$apply$TestObject$2$
    def removeScalaParts(s: String) = s.
      replaceAllLiterally("$$anonfun", ".").
      replaceAllLiterally("$apply", ".").
      replaceAll("""\$\d*""", ".").
      split('.')

    (removeScalaParts(owner.getName) ++ names.filter(_ != null)).filter(_.nonEmpty).mkString(".")
  }
}
