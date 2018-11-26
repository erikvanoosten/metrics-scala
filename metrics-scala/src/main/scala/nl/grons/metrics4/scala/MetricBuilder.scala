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

package nl.grons.metrics4.scala

import java.util.concurrent.atomic.AtomicReference

import com.codahale.metrics.{CachedGauge => DropwizardCachedGauge, Gauge => DropwizardGauge, Metric, MetricFilter, MetricRegistry}
import nl.grons.metrics4.scala.MoreImplicits.RichAtomicReference

import _root_.scala.concurrent.duration.FiniteDuration

/**
 * Builds and registering metrics.
 */
class MetricBuilder(val baseName: MetricName, val registry: MetricRegistry) extends DeprecatedMetricBuilder {

  private[this] val gauges: AtomicReference[Seq[DropwizardGauge[_]]] = new AtomicReference(Seq.empty)

  /**
   * Registers a new gauge metric.
   *
   * @param name the name of the gauge
   */
  def gauge[A](name: String)(f: => A): Gauge[A] = {
    wrapDwGauge(metricNameFor(name), new DropwizardGauge[A] { def getValue: A = f })
  }

  /**
   * Registers a new gauge metric that caches its value for a given duration.
   *
   * @param name the name of the gauge
   * @param timeout the timeout
   */
  def cachedGauge[A](name: String, timeout: FiniteDuration)(f: => A): Gauge[A] = {
    wrapDwGauge(metricNameFor(name), new DropwizardCachedGauge[A](timeout.length, timeout.unit) { def loadValue: A = f })
  }

  private def wrapDwGauge[A](name: String, dwGauge: DropwizardGauge[A]): Gauge[A] = {
    registry.register(name, dwGauge)
    gauges.getAndTransform(_ :+ dwGauge)
    new Gauge[A](dwGauge)
  }

  /**
   * Creates a new counter metric.
   *
   * @param name the name of the counter
   */
  def counter(name: String): Counter =
    new Counter(registry.counter(metricNameFor(name)))

  /**
   * Creates a new histogram metric.
   *
   * @param name the name of the histogram
   */
  def histogram(name: String): Histogram =
    new Histogram(registry.histogram(metricNameFor(name)))

  /**
   * Creates a new meter metric.
   *
   * @param name the name of the meter
   */
  def meter(name: String): Meter =
    new Meter(registry.meter(metricNameFor(name)))

  /**
   * Creates a new timer metric.
   *
   * @param name the name of the timer
   */
  def timer(name: String): Timer =
    new Timer(registry.timer(metricNameFor(name)))

  /**
   * Unregisters all gauges that were created through this builder.
   */
  def unregisterGauges(): Unit = {
    val toUnregister = gauges.getAndTransform(_ => Seq.empty)
    registry.removeMatching(new MetricFilter {
      override def matches(name: String, metric: Metric): Boolean =
        metric.isInstanceOf[DropwizardGauge[_]] && toUnregister.contains(metric)
    })
  }

  protected def metricNameFor(name: String): String = baseName.append(name).name
}

trait DeprecatedMetricBuilder { this: MetricBuilder =>

  /**
    * Registers a new gauge metric.
    *
    * @param name the name of the gauge
    * @param scope (deprecated) the scope of the gauge or null for no scope
    */
  @deprecated("""Please use `gauge(name+"."+scope)(f)` instead. The scope parameter has been deprecated and will be removed in v5.0.0.""", "4.0.3")
  def gauge[A](name: String, scope: String = null)(f: => A): Gauge[A] =
    gauge(mergeScope(name, scope))(f)

  /**
    * Registers a new gauge metric that caches its value for a given duration.
    *
    * @param name the name of the gauge
    * @param timeout the timeout
    * @param scope (deprecated) the scope of the gauge or null for no scope
    */
  @deprecated("""Please use `cachedGauge(name+"."+scope)(f)` instead. The scope parameter has been deprecated and will be removed in v5.0.0.""", "4.0.3")
  def cachedGauge[A](name: String, timeout: FiniteDuration, scope: String = null)(f: => A): Gauge[A] =
    cachedGauge(mergeScope(name, scope), timeout)(f)

  /**
    * Creates a new counter metric.
    *
    * @param name the name of the counter
    * @param scope (deprecated) the scope of the counter or null for no scope
    */
  @deprecated("""Please use `counter(name+"."+scope)(f)` instead. The scope parameter has been deprecated and will be removed in v5.0.0.""", "4.0.3")
  def counter(name: String, scope: String = null): Counter =
    counter(mergeScope(name, scope))

  /**
    * Creates a new histogram metric.
    *
    * @param name the name of the histogram
    * @param scope (deprecated) the scope of the histogram or null for no scope
    */
  @deprecated("""Please use `histogram(name+"."+scope)(f)` instead. The scope parameter has been deprecated and will be removed in v5.0.0.""", "4.0.3")
  def histogram(name: String, scope: String = null): Histogram =
    histogram(mergeScope(name, scope))

  /**
    * Creates a new meter metric.
    *
    * @param name the name of the meter
    * @param scope (deprecated) the scope of the meter or null for no scope
    */
  @deprecated("""Please use `meter(name+"."+scope)(f)` instead. The scope parameter has been deprecated and will be removed in v5.0.0.""", "4.0.3")
  def meter(name: String, scope: String = null): Meter =
    meter(mergeScope(name, scope))

  /**
    * Creates a new timer metric.
    *
    * @param name the name of the timer
    * @param scope (deprecated) the scope of the timer or null for no scope
    */
  @deprecated("""Please use `timer(name+"."+scope)(f)` instead. The scope parameter has been deprecated and will be removed in v5.0.0.""", "4.0.3")
  def timer(name: String, scope: String = null): Timer =
    timer(mergeScope(name, scope))

  private def mergeScope(name: String, scope: String): String =
    if (scope == null) name else name + "." + scope

  @deprecated("""Do not use metricNameFor, it is an internal API. This method will be removed in v5.0.0.""", "4.0.3")
  protected def metricNameFor(name: String, scope: String = null): String =
    baseName.append(name, scope).name

}