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

import com.codahale.metrics.{Meter => CHMeter}

object Meter {
  def apply(metric: CHMeter) = new Meter(metric)
  def unapply(metric: Meter) = Option(metric.metric)
  
  implicit def javaMeter2ScalaMeter(metric: CHMeter) = apply(metric)
  implicit def scalaMeter2JavaMeter(metric: Meter) = metric.metric
}

/**
 * A Scala façade class for Meter.
 */
class Meter(private val metric: CHMeter) {
  
  /**
   * Increments meter on exception
   */
  def exceptionMeter[A](f: => A):A = {
    try {
      f
    } catch {
      case e : Throwable => { 
        metric.mark()
        throw e
      }
    }
  }

  /**
   * Marks the occurrence of an event.
   */
  def mark() {
    metric.mark()
  }

  /**
   * Marks the occurrence of a given number of events.
   */
  def mark(count: Long) {
    metric.mark(count)
  }

  /**
   * The number of events which have been marked.
   */
  def count = metric.getCount()

  /**
   * The fifteen-minute exponentially-weighted moving average rate at
   * which events have occurred since the meter was created.
   * <p>
   * This rate has the same exponential decay factor as the fifteen-minute load
   * average in the top Unix command.
   */
  def fifteenMinuteRate = metric.getFifteenMinuteRate

  /**
   * The five-minute exponentially-weighted moving average rate at
   * which events have occurred since the meter was created.
   * <p>
   * This rate has the same exponential decay factor as the five-minute load
   * average in the top Unix command.
   */
  def fiveMinuteRate = metric.getFiveMinuteRate

  /**
   * The mean rate at which events have occurred since the meter was
   * created.
   */
  def meanRate = metric.getMeanRate

  /**
   * The one-minute exponentially-weighted moving average rate at
   * which events have occurred since the meter was created.
   * <p>
   * This rate has the same exponential decay factor as the one-minute load
   * average in the top Unix command.
   */
  def oneMinuteRate = metric.getOneMinuteRate
}

