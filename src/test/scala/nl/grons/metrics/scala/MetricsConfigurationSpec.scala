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

import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.OneInstancePerTest
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MetricsConfigurationSpec extends FunSpec with MockitoSugar with ShouldMatchers with OneInstancePerTest {

  class UnderTest extends MetricRegistry {
    
    val timer:Timer = timer("10ms")
    val gauge:Gauge[Int] = gauge("the answer")(value)
    val counter:Counter = counter("1..2..3..4")
    val histogram:Histogram = histogram("histo")
    val meter:Meter = meter("meter",Some("testscope"))
    
    def waitFor100Ms = timer.time {
      Thread.sleep(100L)
    }
    
    def value = 42
    
    def incr() = counter += 1
    
    def meterPlusEleven() = meter.mark(11)
    
    def histogramPlusOne() = histogram++
  }
  
  describe("Metrics configuration dsl") {
    
    val underTest = new UnderTest
    
    it("defines a timer") {
      underTest.waitFor100Ms
      underTest.timer.min should be (100000000L plusOrMinus 20000000L)
    }
    
    it("defines a gauge") {
      underTest.gauge.value should equal (42)
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