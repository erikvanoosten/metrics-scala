package com.yammer.metrics.scala

import com.codahale.metrics.MetricRegistry

/**
 * The base class for creating a class which is instrumented with metrics.
 */
trait Instrumented {
  val registry: MetricRegistry
  private lazy val metricsGroup = new MetricsGroup(getClass, metricsRegistry)

  /**
   * Returns the MetricsGroup for the class.
   */
  def metrics = metricsGroup

  /**
   * Returns the MetricsRegistry for the class.
   */
  def metricsRegistry = registry
}

