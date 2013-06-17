package nl.grons.metrics.scala

import org.mockito.Mockito._
import org.scalatest.OneInstancePerTest
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GaugeSpec extends FunSpec with MockitoSugar with ShouldMatchers with OneInstancePerTest {
  describe("A gauge") {
    val metric = mock[com.codahale.metrics.Gauge[Int]]
    val gauge = new Gauge(metric)
    
    it("invokes underlying function for sugar factory") {
      val sugared = Gauge({ 1 })
      
      sugared.value should equal (1)
    }
    
    it("invokes getValue on underlying gauge") {
      when(metric.getValue()).thenReturn(1)
      
      gauge.value should equal (1)
    }
  }
}