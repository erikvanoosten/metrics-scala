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

import java.util.concurrent.atomic.AtomicReference
import com.codahale.metrics.{DefaultSettableGauge, Metric, MetricFilter, MetricRegistry, CachedGauge => DropwizardCachedGauge, Gauge => DropwizardGauge, SettableGauge => DropwizardSettableGauge}
import nl.grons.metrics4.scala.MoreImplicits.RichAtomicReference

import _root_.scala.concurrent.duration.FiniteDuration

/**
 * Builds and registers metrics.
 */
class MetricBuilder(val baseName: MetricName, val registry: MetricRegistry) extends DeprecatedMetricBuilder {

  private[this] val gauges: AtomicReference[Seq[DropwizardGauge[_]]] = new AtomicReference(Seq.empty)

  /**
   * Registers a new gauge metric.
   *
   * Example:
   * {{{
   * import nl.grons.metrics4.scala._
   * class SessionStore(cache: Cache) extends DefaultInstrumented {
   *   // Defines the gauge.
   *   metrics.gauge("cache-evictions") {
   *     // Does a measurement.
   *     cache.getEvictionsCount()
   *   }
   * }
   * }}}
   *
   * @param name the name of the gauge
   * @param f a code block that does a measurement
   * @throws IllegalArgumentException when a metric with the given name already exists
   */
  def gauge[A](name: String)(f: => A): Gauge[A] = {
    registerAndWrapDwGauge(name, new DropwizardGauge[A] { def getValue: A = f })
  }

  /**
   * Registers a new gauge metric that caches its value for a given duration.
   *
   * Example:
   * {{{
   * import nl.grons.metrics4.scala._
   * import scala.concurrent.duration._
   * class UserRepository(db: Database) extends DefaultInstrumented {
   *   // Defines the gauge.
   *   metrics.cachedGauge("row-count", 5.minutes) {
   *     // Does a measurement at most once every 5 minutes.
   *     db.usersRowCount()
   *   }
   * }
   * }}}
   *
   * @param name the name of the gauge
   * @param timeout the timeout
   * @param f a code block that does a measurement
   * @throws IllegalArgumentException when a metric with the given name already exists
   */
  def cachedGauge[A](name: String, timeout: FiniteDuration)(f: => A): Gauge[A] = {
    registerAndWrapDwGauge(
      name,
      new DropwizardCachedGauge[A](timeout.length, timeout.unit) { def loadValue: A = f }
    )
  }

  private def registerAndWrapDwGauge[A](name: String, dwGauge: DropwizardGauge[A]): Gauge[A] = {
    registry.register(metricNameFor(name), dwGauge)
    trackGauge(dwGauge)
    new Gauge[A](dwGauge)
  }

  /**
   * Registers a new gauge metric to which you can push values.
   *
   * Example:
   * {{{
   * import nl.grons.metrics4.scala._
   * class ExternalCacheUpdater extends DefaultInstrumented {
   *   // Defines a push gauge
   *   private val cachedItemsCount = metrics.pushGauge[Int]("cached.items.count", 0)
   *
   *   def updateExternalCache(): Unit = {
   *     val items = fetchItemsFromDatabase()
   *     pushItemsToExternalCache(items)
   *     // Pushes a new measurement to the gauge
   *     cachedItemsCount.push(items.size)
   *     // Alternative way to push a new measurement
   *     cachedItemsCount.value = items.size
   *   }
   * }
   * }}}
   *
   * When a gauge already exists with the given name, parameter `startValue` is ignored and the existing gauge
   * is returned.
   *
   * @param name the name of the gauge
   * @param startValue the first value of the gauge, typically this is `0`, `0L` or `null`.
   */
  def pushGauge[A](name: String, startValue: A): PushGauge[A] = {
    val dwGauge = registry.gauge(
      metricNameFor(name),
      metricSupplier(new DefaultSettableGauge[A](startValue))
    )
    trackGauge(dwGauge)
    new PushGauge[A](dwGauge)
  }

  /**
   * Registers a new gauge metric to which you can push values.
   *
   * The reported value is reset to `defaultValue` after the timeout.
   *
   * Example in which the last pushed measurement is reported for at most 10 minutes.
   * {{{
   * import nl.grons.metrics4.scala._
   * import scala.concurrent.duration._
   * class ExternalCacheUpdater extends DefaultInstrumented {
   *   // Defines a push gauge
   *   private val cachedItemsCount = metrics.pushGaugeWithTimeout[Int]("cached.items.count", 0, 10.minutes)
   *
   *   def updateExternalCache(): Unit = {
   *     val items = fetchItemsFromDatabase()
   *     pushItemsToExternalCache(items)
   *     // Pushes a new measurement to the gauge
   *     cachedItemsCount.push(items.size)
   *     // Alternative way to push a new measurement
   *     cachedItemsCount.value = items.size
   *   }
   * }
   * }}}
   *
   * When a gauge already exists with the given name, parameters `defaultValue` and `timeout` are ignored and the
   * existing gauge is returned.
   *
   * Note: Cleanup of old values happens only on read. In the absence of a metric reporter or other reader, the last
   * pushed value will continue to take space on the heap. As most values are very small (e.g. an `Int`), this should
   * not matter much.
   *
   * @param name the name of the gauge
   * @param defaultValue the first and default value of the gauge, typically this is 0`, `0L` or `null`.
   * @param timeout the timeout
   */
  def pushGaugeWithTimeout[A](name: String, defaultValue: A, timeout: FiniteDuration): PushGaugeWithTimeout[A] = {
    val dwGauge = registry.gauge(
      metricNameFor(name),
      metricSupplier(new DropwizardSettableGaugeWithTimeout[A](timeout, defaultValue))
    )
    trackGauge(dwGauge)
    new PushGaugeWithTimeout(dwGauge)
  }

  //noinspection ConvertExpressionToSAM
  // Work around for Scala 2.11.
  // Once 2.11 support is dropped, `metricSupplier(expr)` can be replaced with `() => expr`.
  private def metricSupplier[A](dw: => DropwizardSettableGauge[A]): MetricRegistry.MetricSupplier[DropwizardSettableGauge[A]] =
    new MetricRegistry.MetricSupplier[DropwizardSettableGauge[A]] {
      override def newMetric(): DropwizardSettableGauge[A] = dw
    }

  private def trackGauge[T](dwGauge: DropwizardGauge[T]): Unit = {
    gauges.getAndTransform(_ :+ dwGauge)
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