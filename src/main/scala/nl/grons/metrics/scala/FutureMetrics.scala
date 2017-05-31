/*
 * Copyright (c) 2013-2017 Erik van Oosten
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

@deprecated(message = "Please use Timer.time() or Timer.timeFuture()", since = "3.5.4")
trait FutureMetrics { self: InstrumentedBuilder =>
  /**
    * @deprecated please use [[Timer.time()]] inside [[Future]] code:
    *
    * Before:
    * {{{
    *   timed("someMetricName") { ... some action ... }
    * }}}
    *
    * After:
    * {{{
    *   val someTimer = metrics.timer("someMetricName")
    *   Future {
    *     someTimer.time { ... some action ... }
    *   }
    * }}}
    */
  @deprecated(message = "Please use Timer.time() inside Future code", since = "3.5.4")
  def timed[A](metricName: String)(action: => A)(implicit context: ExecutionContext): Future[A] = {
    val timer = metrics.timer(metricName)
    Future(timer.time(action))
  }

  /**
    * @deprecated please use [[Timer.timeFuture()]]:
    *
    * Before:
    * {{{
    *   timing("someMetricName") { ... some future ... }
    * }}}
    *
    * After:
    * {{{
    *   val someTimer = metrics.timer("someMetricName")
    *   someTimer.timeFuture { ... some future ... }
    * }}}
    */
  @deprecated(message = "Please use Timer.timeFuture()", since = "3.5.4")
  def timing[A](metricName: String)(future: => Future[A])(implicit context: ExecutionContext): Future[A] = {
    val timer = metrics.timer(metricName)
    val ctx = timer.timerContext()
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
