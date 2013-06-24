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
   * Increments the counter by delta.
   */
  def inc(delta: Long = 1L) {
    this += delta
  }
  
  /**
   * Increments the counter by one.
   */
  def ++ {
    metric.inc(1)
  }
  
  /**
   * Increments the counter by one.
   */
  def inc() {
    this++
  }

  /**
   * Decrements the counter by delta.
   */
  def -=(delta: Long = 1L) {
    metric.dec(delta)
  }

  /**
   * Decrements the counter by delta.
   */
  def dec(delta: Long = 1L) {
    this -= delta
  }
  
  /**
   * Decrements the counter by one.
   */
  def dec() {
    this--
  }
  
  /**
   * Decrements the counter by one.
   */
  def -- {
    metric.dec(1)
  }

  /**
   * The current count.
   */
  def count = metric.getCount

}
