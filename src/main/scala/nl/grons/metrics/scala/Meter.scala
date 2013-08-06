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
import scala.util.control.ControlThrowable

/**
 * A Scala façade class for Meter.
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
class Meter(private val metric: CHMeter) {

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
   * Gives a marker that wraps a PartialFunction pf, which on execution marks the meter on an exception, and returns result of pf.
   *
   * Example usage:
   * {{{
   *  class Example(val shardedDb: List[Db]) extends Instrumented {
   *    private[this] val shardExceptionMeter = metrics.meter("shard").exceptionMarkerPF
   *
   *    private[this] val shardFunction: PartialFunction[Long,Db] = shardExceptionMeter {
   *      case id: Long => shardedDb(id.toInt % shardedDb.length)
   *    }
   *
   *    private[this] def shard(id: Long): Db = shardFunction.applyOrElse(id,(x: Long) => shardedDb(0))
   *
   *    def load(id: Long) = {
   *      shard(id).load(id)
   *    }
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

  @deprecated("please use exceptionMarkerPF", "3.0.2")
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
