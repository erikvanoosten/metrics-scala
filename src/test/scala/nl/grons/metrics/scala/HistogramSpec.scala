package nl.grons.metrics.scala

import org.mockito.Mockito._
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.OneInstancePerTest

@RunWith(classOf[JUnitRunner])
class HistogramSpec extends FunSpec with MockitoSugar with ShouldMatchers with OneInstancePerTest {

  describe("A histogram") {
    
    val metric = mock[com.codahale.metrics.Histogram]
    val histogram = new Histogram(metric)
    
    it("updates the underlying histogram with an int") {
      histogram += 12
      
      verify(metric).update(12)
    }
    
    it("updates the underlying histogram with a long") {
      histogram += 12L
      
      verify(metric).update(12L)
    }
    
    it("increments the underlying histogram with 1") {
      histogram++
      
      verify(metric).update(1)
    }
    
    it("retrieves a snapshot for statistics") {
      val snapshot = mock[com.codahale.metrics.Snapshot]
      when(snapshot.getMax()).thenReturn(1L)
      when(metric.getSnapshot()).thenReturn(snapshot)
      
      histogram.max should equal (1L)
    }
    
  }
  
}