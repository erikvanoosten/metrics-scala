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
   * The number of values recorded.
   */
  def count = metric.getCount()

  /**
   * The largest recorded value.
   */
  def max = snapshot.getMax()

  /**
   * The smallest recorded value.
   */
  def min = snapshot.getMin()

  /**
   * The arithmetic mean of all recorded values.
   */
  def mean = snapshot.getMean()

  /**
   * The standard deviation of all recorded values.
   */
  def stdDev = snapshot.getStdDev()

  /**
   * A snapshot of the values in the histogram's sample.
   */
  def snapshot = metric.getSnapshot
}

