/*
 * Copyright (c) 2013-2015 Erik van Oosten
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

import com.codahale.metrics.{Meter => DropwizardMeter}
import scala.util.control.ControlThrowable

/**
 * A Scala facade class for [[DropwizardMeter]].
 *
 * Example usage:
 * {{{
 *   class Example(val db: Db) extends Instrumented {
 *     private[this] val rowsLoadedMeter = metrics.meter("rowsLoaded")
 *
 *     def load(id: Long): Seq[Row] = {
 *       val rows = db.load(id)
 *       rowsLoaded.mark(rows.size)
 *       rows
 *     }
 *   }
 * }}}
 */
class Meter(metric: DropwizardMeter) {

  /**
   * Gives a marker that runs f, marks the meter on an exception, and returns result of f.
   *
   * Example usage:
   * {{{
   *   class Example(val db: Db) extends Instrumented {
   *     private[this] val loadExceptionMeter = metrics.meter("load").exceptionMarker
   *
   *     def load(id: Long) = loadExceptionMeter {
   *       db.load(id)
   *     }
   *   }
   * }}}
   */
  def exceptionMarker = new AnyRef() {
    def apply[A](f: => A): A = {
        try {
          f
        } catch {
          case e: ControlThrowable =>
            // ControlThrowable is used by Scala for control, it is equivalent to success.
            throw e
          case e: Throwable =>
            metric.mark()
            throw e
        }
      }
  }

  /**
   * Converts partial function `pf` into a side-effecting partial function that meters
   * thrown exceptions for every invocation of `pf` (for the cases it is defined).
   * The result is passed unchanged.
   *
   * Example usage:
   * {{{
   *  class Example extends Instrumented {
   *    val isEven: PartialFunction[Int, String] = {
   *      case x if x % 2 == 0 => x+" is even"
   *      case 5 => throw new IllegalArgumentException("5 is unlucky")
   *    }
   *
   *    val isEvenExceptionMeter = metrics.meter("isEvenExceptions")
   *    val meteredIsEven: PartialFunction[Int, String] = isEvenExceptionMeter.exceptionMarkerPF(isEven)
   *
   *    val sample = 1 to 10
   *    sample collect meteredIsEven   // the meter counts 1 exception
   *  }
   * }}}
   */
  def exceptionMarkerPF = new AnyRef() {
    def apply[A, B](pf: PartialFunction[A, B]): PartialFunction[A, B] =
      new PartialFunction[A, B] {
        def apply(a: A): B = {
          try {
            pf.apply(a)
          } catch {
            case e: Throwable => mark(); throw e
          }
        }

        def isDefinedAt(a: A) = pf.isDefinedAt(a)
      }
  }

  @deprecated("please use exceptionMarkerPF", since = "3.0.2")
  @inline
  def exceptionMarkerPartialFunction = exceptionMarkerPF

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
  def count: Long = metric.getCount

  /**
   * The fifteen-minute exponentially-weighted moving average rate at
   * which events have occurred since the meter was created.
   * <p>
   * This rate has the same exponential decay factor as the fifteen-minute load
   * average in the top Unix command.
   */
  def fifteenMinuteRate: Double = metric.getFifteenMinuteRate

  /**
   * The five-minute exponentially-weighted moving average rate at
   * which events have occurred since the meter was created.
   * <p>
   * This rate has the same exponential decay factor as the five-minute load
   * average in the top Unix command.
   */
  def fiveMinuteRate: Double = metric.getFiveMinuteRate

  /**
   * The mean rate at which events have occurred since the meter was
   * created.
   */
  def meanRate: Double = metric.getMeanRate

  /**
   * The one-minute exponentially-weighted moving average rate at
   * which events have occurred since the meter was created.
   * <p>
   * This rate has the same exponential decay factor as the one-minute load
   * average in the top Unix command.
   */
  def oneMinuteRate: Double = metric.getOneMinuteRate
}
