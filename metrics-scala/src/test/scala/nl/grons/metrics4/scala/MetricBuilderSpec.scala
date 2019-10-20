/*
 * Copyright (c) 2013-2019 Erik van Oosten
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

package nl.grons.metrics4.scala

import com.codahale.metrics.MetricRegistry
import org.scalatest.OneInstancePerTest
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._

import scala.concurrent.duration._

class MetricBuilderSpec extends AnyFunSpec with OneInstancePerTest {

  private val testMetricRegistry = new MetricRegistry()

  private trait Instrumented extends InstrumentedBuilder {
    val metricRegistry: MetricRegistry = testMetricRegistry
  }

  private class UnderTest extends Instrumented {
    val timer: Timer = metrics.timer("10ms")
    val gauge: Gauge[Int] = metrics.gauge("the answer")(value)
    val cachedGauge: Gauge[Int] = metrics.cachedGauge("cached", 300.milliseconds)(expensiveValue)
    val counter: Counter = metrics.counter("1..2..3..4")
    val histogram: Histogram = metrics.histogram("histo")
    //noinspection ScalaDeprecation
    val meter: Meter = metrics.meter("meter", "testscope")

    def waitFor100Ms(): Unit = {
      timer.time {
        Thread.sleep(100L)
      }
    }

    def value = 42

    var expensiveValueInvocations = 0

    def expensiveValue: Int = {
      expensiveValueInvocations += 1
      42
    }

    def incr(): Unit = { counter += 1 }

    def meterPlusEleven(): Unit = { meter.mark(11) }

    def histogramPlusOne(): Unit = { histogram += 1 }
  }

  describe("Metrics configuration dsl") {
    val underTest = new UnderTest

    it("defines a timer") {
      underTest.waitFor100Ms()
      underTest.timer.min should be >= 100000000L
    }

    it("defines a gauge") {
      underTest.gauge.value should equal (42)
    }

    it("defines a cached gauge") {
      underTest.expensiveValueInvocations should equal (0)
      underTest.cachedGauge.value should equal (42)
      underTest.expensiveValueInvocations should equal (1)
      underTest.cachedGauge.value should equal (42)
      underTest.expensiveValueInvocations should equal (1)
      Thread.sleep(400L)
      underTest.cachedGauge.value should equal (42)
      underTest.expensiveValueInvocations should equal (2)
    }

    it("defines a counter") {
      underTest.incr()
      underTest.counter.count should equal (1)
    }

    it("defines a histogram") {
      underTest.histogramPlusOne()
      underTest.histogram.count should equal (1)
      underTest.histogram.min should equal (1)
    }

    it("defines a meter") {
      underTest.meterPlusEleven()
      underTest.meter.count should equal (11)
    }
  }

}
