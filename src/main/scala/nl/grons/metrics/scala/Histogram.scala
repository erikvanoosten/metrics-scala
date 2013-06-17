package nl.grons.metrics.scala

import com.codahale.metrics.{Histogram => CHHistogram}

object Histogram {
  def apply(metric: CHHistogram) = new Histogram(metric)
  def unapply(metric: Histogram) = Option(metric.metric)
  
  implicit def javaHistogram2ScalaHistogram(metric: CHHistogram) = apply(metric)
  implicit def scalaHistogram2JavaHistogram(metric: Histogram) = metric.metric
}

/**
 * A Scala façade class for HistogramMetric.
 *
 * @see HistogramMetric
 */
class Histogram(private val metric: CHHistogram) {

  /**
   * Adds the recorded value to the histogram sample.
   */
  def +=(value: Long) {
    metric.update(value)
  }

  /**
   * Adds the recorded value to the histogram sample.
   */
  def +=(value: Int) {
    metric.update(value)
  }
  
  /**
   * Adds one to the histogram sample.
   */
  def ++ {
    metric.update(1)
  }

  /**
   * Returns the number of values recorded.
   */
  def count = metric.getCount()

  /**
   * Returns the largest recorded value.
   */
  def max = snapshot.getMax()

  /**
   * Returns the smallest recorded value.
   */
  def min = snapshot.getMin()

  /**
   * Returns the arithmetic mean of all recorded values.
   */
  def mean = snapshot.getMean()

  /**
   * Returns the standard deviation of all recorded values.
   */
  def stdDev = snapshot.getStdDev()

  /**
   * Returns a snapshot of the values in the histogram's sample.
   */
  def snapshot = metric.getSnapshot
}

