package com.yammer.metrics.scala

object Counter {
  def apply(metric: com.codahale.metrics.Counter) = new Counter(metric)
  def unapply(metric: Counter) = Option(metric.metric)
  
  implicit def javaCounter2ScalaCounter(metric: com.codahale.metrics.Counter) = apply(metric)
  implicit def scalaCounter2JavaCounter(metric: Counter) = metric.metric
}

/**
 * A Scala façade class for Counter.
 */
class Counter(private val metric: com.codahale.metrics.Counter) {

  /**
   * Increments the counter by delta.
   */
  def +=(delta: Long = 1L) {
    metric.inc(delta)
  }

  /**
   * Decrements the counter by delta.
   */
  def -=(delta: Long = 1L) {
    metric.dec(delta)
  }

  /**
   * Returns the current count.
   */
  def count = metric.getCount

}
