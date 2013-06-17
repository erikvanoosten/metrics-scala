package nl.grons.metrics.scala

import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.OneInstancePerTest
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import java.io.File

@RunWith(classOf[JUnitRunner])
class MetricsConfigurationSpec extends FunSpec with MockitoSugar with ShouldMatchers with OneInstancePerTest {

  class UnderTest extends MetricRegistry {
    
    val timer:Timer = timer("10ms")
    val gauge:Gauge[Int] = gauge("the answer")(value)
    val counter:Counter = counter("1..2..3..4")
    val histogram:Histogram = histogram("histo")
    val meter:Meter = meter("meter",Some("testscope"))
    
    def waitFor1Ms = timer.time {
      Thread.sleep(1L)
    }
    
    def value = 42
    
    def incr() = counter += 1
    
    def meterPlusEleven() = meter.mark(11)
    
    def histogramPlusOne() = histogram++
  }
  
  describe("Metrics configuration dsl") {
    
    val underTest = new UnderTest
    
    it("defines a timer") {
      underTest.waitFor1Ms
      underTest.timer.min should be (1000000L plusOrMinus 200000L)
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