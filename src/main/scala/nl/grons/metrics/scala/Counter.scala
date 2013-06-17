package nl.grons.metrics.scala

import com.codahale.metrics.{Counter => CHCounter}

object Counter {
  def apply(metric: CHCounter) = new Counter(metric)
  def unapply(metric: Counter) = Option(metric.delegate)
  
  implicit def javaCounter2ScalaCounter(metric: CHCounter) = apply(metric)
  implicit def scalaCounter2JavaCounter(metric: Counter) = metric.delegate
}

/**
 * A Scala façade class for Counter.
 */
class Counter(metric: CHCounter) {
  
  private def delegate = metric

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
