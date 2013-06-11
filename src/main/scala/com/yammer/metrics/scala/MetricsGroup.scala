package com.yammer.metrics.scala

import java.util.concurrent.TimeUnit
import com.codahale.metrics.SharedMetricRegistries
import com.codahale.metrics.{MetricRegistry, Gauge}

/**
 * A helper class for creating and registering metrics.
 */
class MetricsGroup(val klass: Class[_], val metricsRegistry: MetricRegistry) {
  
  private[this] def metricName(name: String, maybeScope: Option[String] = None) = maybeScope match {
      case Some(scope) => MetricRegistry.name(klass,name,scope)
      case None => MetricRegistry.name(klass,name)
  }

  /**
   * Registers a new gauge metric.
   *
   * @param name  the name of the gauge
   * @param scope the scope of the gauge
   * @param registry the registry for the gauge
   */
  def gauge[A](name: String, maybeScope: Option[String] = None, registry: MetricRegistry = metricsRegistry)(f: => A) =
    registry.register(metricName(name,maybeScope), new Gauge[A] { def getValue = f })

  /**
   * Creates a new counter metric.
   *
   * @param name  the name of the counter
   * @param scope the scope of the gauge
   * @param registry the registry for the gauge
   */
  def counter(name: String, maybeScope: Option[String] = None, registry: MetricRegistry = metricsRegistry) = 
		  new Counter(registry.counter(metricName(name, maybeScope)))

  /**
   * Creates a new histogram metrics.
   *
   * @param name   the name of the histogram
   * @param scope  the scope of the histogram
   * @param registry the registry for the gauge
   */
  def histogram(name: String,
                maybeScope: Option[String] = None,
                registry: MetricRegistry = metricsRegistry) =
    new Histogram(registry.histogram(metricName(name, maybeScope)))

  /**
   * Creates a new meter metric.
   *
   * @param name the name of the meter
   * @param eventType the plural name of the type of events the meter is
   *                  measuring (e.g., "requests")
   * @param scope the scope of the meter
   * @param registry the registry for the gauge
   */
  def meter(name: String,
            maybeScope: Option[String] = None,
            registry: MetricRegistry = metricsRegistry) =
    new Meter(registry.meter(metricName(name, maybeScope)))

  /**
   * Creates a new timer metric.
   *
   * @param name the name of the timer
   * @param scope the scope of the timer
   * @param registry the registry for the gauge
   */
  def timer(name: String,
            maybeScope: Option[String] = None,
            registry: MetricRegistry = metricsRegistry) =
    new Timer(registry.timer(metricName(name, maybeScope)))
}

