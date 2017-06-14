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

import com.codahale.metrics.MetricRegistry
import org.scalatest.Matchers._
import org.scalatest.{AsyncFunSpec, FunSpec, Inspectors, OneInstancePerTest}

import scala.concurrent.{ExecutionContext, Future}

class HdrMetricBuilderSpec extends AsyncFunSpec with OneInstancePerTest with Inspectors {
  private val testMetricRegistry = new MetricRegistry()

  private trait Instrumented extends InstrumentedBuilder {
    override lazy protected val metricBuilder = new HdrMetricBuilder(metricBaseName, metricRegistry, resetAtSnapshot = false)
    val metricRegistry = testMetricRegistry
  }

  private trait InstrumentedWithReset extends InstrumentedBuilder {
    override lazy protected val metricBuilder = new HdrMetricBuilder(metricBaseName, metricRegistry, resetAtSnapshot = true)
    val metricRegistry = testMetricRegistry
  }

  private class UnderTest extends Instrumented {
    val timer: Timer = metrics.timer("10ms")
    val histogram: Histogram = metrics.histogram("histo")

    def createTimer(name: String): Timer = metrics.timer(name)
    def createHistogram(name: String): Histogram = metrics.histogram(name)
  }

  private class UnderTestWithReset extends InstrumentedWithReset {
    val timer: Timer = metrics.timer("10ms")
    val histogram: Histogram = metrics.histogram("histo")
  }

  def waitFor100Ms(timer: Timer) {
    timer.time {
      Thread.sleep(100L)
    }
  }

  describe("Metrics configuration dsl with HdrMetricBuilder") {
    val underTest = new UnderTest

    it("defines a timer") {
      waitFor100Ms(underTest.timer)
      underTest.timer.max should be >= 80000000L
    }

    it("defines a histogram") {
      underTest.histogram += 1
      underTest.histogram.count should equal (1)
      underTest.histogram.min should equal (1)
    }

    it("allows a timer to be 'created' twice") {
      val timer1 = underTest.createTimer("test.timer")
      val timer2 = underTest.createTimer("test.timer")
      timer1.metric should be theSameInstanceAs(timer2.metric)
    }

    it("allows a histogram to be 'created' twice") {
      val histogram1 = underTest.createHistogram("test.histogram")
      val histogram2 = underTest.createHistogram("test.histogram")
      histogram1.metric should be theSameInstanceAs(histogram2.metric)
    }

    it("allows identical histograms to be 'created' concurrently") {
      implicit val ec = ExecutionContext.global
      val histogramsF = List.fill(30)(Future{ underTest.createHistogram("test.histogram")} )
      Future.sequence(histogramsF).map{ histograms =>
        val head = histograms.head
        forAll (histograms) { h => head.metric should be theSameInstanceAs(h.metric) }
      }
    }

    it ("gives IllegalArgumentException when second creation is of different type") {
      underTest.createTimer("test.metric")
      val thrown = the [IllegalArgumentException] thrownBy underTest.createHistogram("test.metric")
      thrown.getMessage should equal ("Already existing metric 'nl.grons.metrics.scala.HdrMetricBuilderSpec.UnderTest.test.metric' is of type Timer, expected a Histogram")
    }

    it("defines a timer with non-resetting reservoir") {
      waitFor100Ms(underTest.timer)
      underTest.timer.max should be >= 80000000L
      underTest.timer.max should be >= 80000000L
    }

    it("defines a histogram with non-resetting reservoir") {
      underTest.histogram += 1
      underTest.histogram.max should equal (1)
      underTest.histogram.max should equal (1)
    }
  }

  describe("Metrics configuration dsl with HdrMetricBuilder with reset") {
    val underTest = new UnderTestWithReset

    it("defines a timer with resetting reservoir") {
      waitFor100Ms(underTest.timer)
      underTest.timer.max should be >= 80000000L
      underTest.timer.max should be (0)
    }

    it("defines a histogram with resetting reservoir") {
      underTest.histogram += 1
      underTest.histogram.max should equal (1)
      underTest.histogram.max should equal (0)
    }
  }
}
