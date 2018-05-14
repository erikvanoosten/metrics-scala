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

import java.util.concurrent.atomic.AtomicReference

import io.dropwizard.metrics5.{CachedGauge => DropwizardCachedGauge, Gauge => DropwizardGauge, Metric, MetricFilter, MetricName, MetricRegistry}
import nl.grons.metrics5.scala.MoreImplicits.RichAtomicReference

import _root_.scala.concurrent.duration.FiniteDuration
import _root_.scala.collection.JavaConverters._

/**
 * Builds and registering metrics.
 */
class MetricBuilder(val baseName: MetricName, val registry: MetricRegistry) {

  private[this] val gauges: AtomicReference[Seq[DropwizardGauge[_]]] = new AtomicReference(Seq.empty)

  /**
    * Registers a new gauge metric.
    *
    * @param name the name of the gauge
    * @param tags any tags to use on this metric
    */
  def gauge[A](name: String, tags: Map[String, String] = Map.empty)(f: => A): Gauge[A] = {
    wrapDwGauge(metricNameFor(name, tags), new DropwizardGauge[A] { def getValue: A = f })
  }

  /**
    * Registers a new gauge metric that caches its value for a given duration.
    *
    * @param name the name of the gauge
    * @param timeout the timeout
    * @param tags any tags to use on this metric
    */
  def cachedGauge[A](name: String, timeout: FiniteDuration, tags: Map[String, String] = Map.empty)(f: => A): Gauge[A] = {
    wrapDwGauge(metricNameFor(name, tags), new DropwizardCachedGauge[A](timeout.length, timeout.unit) { def loadValue: A = f })
  }

  private def wrapDwGauge[A](name: MetricName, dwGauge: DropwizardGauge[A]): Gauge[A] = {
    registry.register(name, dwGauge)
    gauges.getAndTransform(_ :+ dwGauge)
    new Gauge[A](dwGauge)
  }

  /**
    * Creates a new counter metric.
    *
    * @param name the name of the counter
    * @param tags any tags to use on this metric
    */
  def counter(name: String, tags: Map[String, String] = Map.empty): Counter =
    new Counter(registry.counter(metricNameFor(name, tags)))

  /**
    * Creates a new histogram metric.
    *
    * @param name the name of the histogram
    * @param tags any tags to use on this metric
    */
  def histogram(name: String, tags: Map[String, String] = Map.empty): Histogram =
    new Histogram(registry.histogram(metricNameFor(name, tags)))

  /**
    * Creates a new meter metric.
    *
    * @param name the name of the meter
    * @param tags any tags to use on this metric
    */
  def meter(name: String, tags: Map[String, String] = Map.empty): Meter =
    new Meter(registry.meter(metricNameFor(name, tags)))

  /**
    * Creates a new timer metric.
    *
    * @param name the name of the timer
    * @param tags any tags to use on this metric
    */
  def timer(name: String, tags: Map[String, String] = Map.empty): Timer =
    new Timer(registry.timer(metricNameFor(name, tags)))

  /**
    * Unregisters all gauges that were created through this builder.
    */
  def unregisterGauges(): Unit = {
    val toUnregister = gauges.getAndTransform(_ => Seq.empty)
    registry.removeMatching(new MetricFilter {
      override def matches(name: MetricName, metric: Metric): Boolean =
        metric.isInstanceOf[DropwizardGauge[_]] && toUnregister.contains(metric)
    })
  }

  protected def metricNameFor(name: String, tags: Map[String, String]): MetricName =
    baseName.resolve(name).tagged(tags.asJava)
}
