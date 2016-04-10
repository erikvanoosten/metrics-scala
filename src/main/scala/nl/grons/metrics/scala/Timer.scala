/*
 * Copyright (c) 2013-2016 Erik van Oosten
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

import com.codahale.metrics.{Snapshot, Timer => DropwizardTimer}
import java.util.concurrent.TimeUnit

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

/**
  * A Scala facade class for [[DropwizardTimer]].
  *
  * Features:
  * * measure the execution duration of a block of code with [[time()]]
  * * measure the time until a future is completed with [[timeFuture()]]
  * * add an execution duration measurement as a side effect to a partial function with [[timePF()]]
  * * direct access to the underlying timer with [[update()]], [[timerContext()]], [[count]], [[max]], etc.
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
class Timer(private[scala] val metric: DropwizardTimer) {

  /**
   * Runs f, recording its duration, and returns its result.
   */
  def time[A](f: => A): A = {
    val ctx = metric.time()
    try {
      f
    } finally {
      ctx.stop
    }
  }

  /**
    * Measures 'now' up to the moment that the given `future` completes, then updates this timer with the measurement.
    *
    * *Know what you measure*
    *
    * This method may measure more than is obvious. It measures:
    * * the evaluation of the (by name) parameter `future`
    * * in case the future is not yet completed: the delay until the constructed Future is scheduled in the
    *   given `ExecutionContext`
    * * in case the future is not yet completed: the actual execution of the Future
    * * the time it takes to schedule stopping the timer
    *
    * To only measure the Future execution time, please use use a timer in the code that is executed inside the Future.
    *
    * Example usage:
    * {{{
    * class Example extends Instrumented {
    *   private[this] loadTimer = metrics.timer("loading")
    *
    *   private def asyncFetchRows(): Future[Seq[Row]] = ...
    *
    *   def loadStuffEventually(): Future[Seq[Row]] = loadTimer.timeFuture { asyncFetchRows() }
    * }
    * }}}
    *
    * @param future the expression that results in a future
    * @param context execution context
    * @tparam A future result type
    * @return the result of executing `future`
    */
  def timeFuture[A](future: => Future[A])(implicit context: ExecutionContext): Future[A] = {
    val ctx = metric.time()
    val f = future
    f.onComplete(_ => ctx.stop())
    f
  }

  /**
   * Converts partial function `pf` into a side-effecting partial function that times
   * every invocation of `pf` for which it is defined. The result is passed unchanged.
   *
   * Example usage:
   * {{{
   *  class Example extends Instrumented {
   *    val isEven: PartialFunction[Int, String] = {
   *      case x if x % 2 == 0 => x+" is even"
   *    }
   *
   *    val isEvenTimer = metrics.timer("isEven")
   *    val timedIsEven: PartialFunction[Int, String] = isEvenTimer.timePF(isEven)
   *
   *    val sample = 1 to 10
   *    sample collect timedIsEven   // timer does 5 measurements
   *  }
   * }}}
   */
  def timePF[A,B](pf: PartialFunction[A,B]): PartialFunction[A,B] = new PartialFunction[A,B] {
    def apply(a: A): B = {
        val ctx = metric.time()
        try {
          pf.apply(a)
        } finally {
          ctx.stop()
        }
    }

    def isDefinedAt(a: A) = pf.isDefinedAt(a)
  }

  /**
   * Adds a recorded duration.
   */
  def update(duration: FiniteDuration) {
    metric.update(duration.length, duration.unit)
  }

  /**
   * Adds a recorded duration.
   */
  def update(duration: Long, unit: TimeUnit) {
    metric.update(duration, unit)
  }

  /**
   * A timing [[com.codahale.metrics.Timer.Context]],
   * which measures an elapsed time in nanoseconds.
   */
  def timerContext(): DropwizardTimer.Context = metric.time()

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
