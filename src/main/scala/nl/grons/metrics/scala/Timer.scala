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

import java.util.concurrent.TimeUnit
import com.codahale.metrics.{Timer => CHTimer}

object Timer {
  def apply(metric: CHTimer) = new Timer(metric)
  def unapply(metric: Timer) = Option(metric.metric)
  
  implicit def javaTimer2ScalaTimer(metric: CHTimer) = apply(metric)
  implicit def scalaTimer2JavaTimer(metric: Timer) = metric.metric
}

/**
 * A Scala façade class for Timer.
 */
class Timer(private val metric: CHTimer) {
  /**
   * Runs f, recording its duration, and returns the result of f.
   */
  def time[A](f: => A): A = {
    val ctx = metric.time
    try {
      f
    } finally {
      ctx.stop
    }
  }

  /**
   * Adds a recorded duration.
   */
  def update(duration: Long, unit: TimeUnit) {
    metric.update(duration, unit)
  }

  /**
   * A timing [[com.metrics.yammer.core.TimerContext]],
   * which measures an elapsed time in nanoseconds.
   */
  def timerContext() = metric.time()

  /**
   * The number of durations recorded.
   */
  def count = metric.getCount

  /**
   * The longest recorded duration.
   */
  def max = snapshot.getMax

  /**
   * The shortest recorded duration.
   */
  def min = snapshot.getMin

  /**
   * The arithmetic mean of all recorded durations.
   */
  def mean = snapshot.getMean

  /**
   * The standard deviation of all recorded durations.
   */
  def stdDev = snapshot.getStdDev

  /**
   * A snapshot of the values in the timer's sample.
   */
  def snapshot = metric.getSnapshot

  /**
   * The fifteen-minute rate of timings.
   */
  def fifteenMinuteRate = metric.getFifteenMinuteRate

  /**
   * The five-minute rate of timings.
   */
  def fiveMinuteRate = metric.getFiveMinuteRate

  /**
   * The mean rate of timings.
   */
  def meanRate = metric.getMeanRate

  /**
   * The one-minute rate of timings.
   */
  def oneMinuteRate = metric.getOneMinuteRate
}

