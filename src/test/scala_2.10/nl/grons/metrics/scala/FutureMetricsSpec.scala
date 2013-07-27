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

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.OneInstancePerTest
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.FunSpec
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.{Timer => CHTimer}
import com.codahale.metrics.Timer.Context

@RunWith(classOf[JUnitRunner])
class FutureMetricsSpec extends FunSpec with ShouldMatchers with OneInstancePerTest
		with FutureMetrics with InstrumentedBuilder {

  import MockitoSugar._
  import Matchers._

  val metricRegistry = null
  override def metrics = new MetricBuilder(null,null) {
    override def timer(name: String, scope: String = null): Timer = mockTimer
  }

  var timeCalled = false
  val mockTimer = new Timer(null) {
    override def time[A](action: => A): A = { timeCalled = true; action }
    override def timerContext = mockTimerContext
  }
  val mockTimerContext = mock[Context]

  implicit def ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor)

  describe("A future timer") {
    it("should time an execution") {
      val f = timed("test") {
        Thread.sleep(10L)
        10
      }
      val result = Await.result(f, Duration(300L,TimeUnit.MILLISECONDS))
      timeCalled should be (true)
      result should be (10)
    }
  }

}