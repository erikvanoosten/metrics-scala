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

import org.mockito.Mockito.verify
import org.scalatest.OneInstancePerTest
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar._
import org.scalatest.FunSpec
import scala.concurrent.ExecutionContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.concurrent.Await
import com.codahale.metrics.Timer.Context
import scala.concurrent.Promise
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class FutureMetricsSpec extends FunSpec with OneInstancePerTest with FutureMetrics with InstrumentedBuilder {

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

  implicit def sameThreadEc: ExecutionContext = new ExecutionContext {
    def execute(runnable: Runnable): Unit = runnable.run
    def reportFailure(t: Throwable): Unit = throw t
  }

  describe("A future timer") {
    it("should time an execution") {
      val f = timed("test") {
        Thread.sleep(10L)
        10
      }
      val result = Await.result(f, 300.millis)
      timeCalled should be (true)
      result should be (10)
    }

    it("should attach an onComplete listener") {
      val p = Promise[String]()
      var invocationCount = 0
      val f = timing("test") {
        invocationCount += 1
        p.future
      }
      p.success("test")
      val result = Await.result(f, 50.millis)
      result should be ("test")
      verify(mockTimerContext).stop()
      invocationCount should be (1)
    }
  }

}
