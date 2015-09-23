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

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

/**
 * Provides timing of future executions.
 */
trait FutureMetrics { self: InstrumentedBuilder =>
  /**
   * Creates a future that executes the given `action` and times it.
   *
   * Use it as follows:
   * {{{
   * object Application {
   *   // The application wide metrics registry.
   *   val metricRegistry = new com.codahale.metrics.MetricRegistry()
   * }
   * trait Instrumented extends InstrumentedBuilder with FutureMetrics {
   *   val metricRegistry = Application.metricRegistry
   * }
   *
   * case class Row(record: Map[String,Any])
   *
   * trait SynchronousDb {
   *   def fetchRows(): Seq[Row]
   * }
   *
   * class Example(db: Database) extends Instrumented {
   *   import scala.concurrent._
   *   import ExecutionContext.Implicits.global
   *
   *   def loadStuffEventually()(implicit db: SynchronousDb): Future[Seq[Row]] = timed("loading") {
   *     db.fetchRows()
   *   }
   * }
   * }}}
   */
  def timed[A](metricName: String)(action: => A)(implicit context: ExecutionContext): Future[A] = {
    val timer = metrics.timer(metricName)
    Future(timer.time(action))
  }

  /**
   * Starts a timer that stops when the given `future` completes.
   *
   * An important point is that the timer does not measure the exact execution time of the `future`, unlike `timed`.
   * The future might not have been scheduled, or it could have been completed the moment `timing` is called. The
   * `onComplete` listener also needs to be scheduled. If you need exact timings, please make sure to use a timer
   * inside the future's execution.
   *
   * Use it as follows:
   * {{{
   * object Application {
   *   // The application wide metrics registry.
   *   val metricRegistry = new com.codahale.metrics.MetricRegistry()
   * }
   * trait Instrumented extends InstrumentedBuilder with FutureMetrics {
   *   val metricRegistry = Application.metricRegistry
   * }
   *
   * case class Row(record: Map[String,Any])
   *
   * trait AsynchronousDb {
   *   def fetchRows(): Future[Seq[Row]]
   * }
   *
   * class Example(db: Database) extends Instrumented {
   *   import scala.concurrent._
   *   import ExecutionContext.Implicits.global
   *
   *   def loadStuffEventually()(implicit db: AsynchronousDb): Future[Seq[Row]] = timing("loading") {
   *     db.fetchRows()
   *   }
   * }
   * }}}
   */
  def timing[A](metricName: String)(future: => Future[A])(implicit context: ExecutionContext): Future[A] = {
    val timer = metrics.timer(metricName)
    val ctx = timer.timerContext
    val f = future
    f.onComplete(_ => ctx.stop())
    f
  }
}

object FutureMetrics {
  def timed[A](metricName: String)(action: => A)
              (implicit ec: ExecutionContext, metrics: MetricBuilder): Future[A] = {
    val timer = metrics.timer(metricName)
    Future(timer.time(action))
  }

  def timing[A](metricName: String)(future: => Future[A])
               (implicit ec: ExecutionContext, metrics: MetricBuilder): Future[A] = {
    val timer = metrics.timer(metricName)
    val ctx = timer.timerContext()
    val f = future
    f.onComplete(_ => ctx.stop())
    f
  }
}
