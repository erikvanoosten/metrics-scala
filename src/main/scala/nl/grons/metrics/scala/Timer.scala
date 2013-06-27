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
import com.codahale.metrics.{Timer => CHTimer, Snapshot}

object Timer {
  def apply(metric: CHTimer) = new Timer(metric)
  def unapply(metric: Timer) = Option(metric.metric)

  implicit def javaTimer2ScalaTimer(metric: CHTimer) = apply(metric)
  implicit def scalaTimer2JavaTimer(metric: Timer) = metric.metric
}

/**
 * A Scala façade class for Timer.
 *
 * Example usage:
 * {{{
 *   class Example(val db: Db) extends Instrumented {
 *     private[this] val loadTimer = metrics.timer("load")
 *
 *     def load(id: Long) = loadTimer.time {
 *       db.load(id)
 *     }
 *   }
 * }}}
 */
class Timer(private val metric: CHTimer) {

  /**
   * Runs f, recording its duration, and returns its result.
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
  def count: Long = metric.getCount

  /**
   * The longest recorded duration in nanoseconds.
   */
  def max: Long = snapshot.getMax

  /**
   * The shortest recorded duration in nanoseconds.
   */
  def min: Long = snapshot.getMin

  /**
   * The arithmetic mean of all recorded durations in nanoseconds.
   */
  def mean: Double = snapshot.getMean

  /**
   * The standard deviation of all recorded durations.
   */
  def stdDev: Double = snapshot.getStdDev

  /**
   * A snapshot of the values in the timer's sample.
   */
  def snapshot: Snapshot = metric.getSnapshot

  /**
   * The fifteen-minute rate of timings.
   */
  def fifteenMinuteRate: Double = metric.getFifteenMinuteRate

  /**
   * The five-minute rate of timings.
   */
  def fiveMinuteRate: Double = metric.getFiveMinuteRate

  /**
   * The mean rate of timings.
   */
  def meanRate: Double = metric.getMeanRate

  /**
   * The one-minute rate of timings.
   */
  def oneMinuteRate: Double = metric.getOneMinuteRate
}
