package nl.grons.metrics.scala

import org.mockito.Mockito._
import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.OneInstancePerTest

@RunWith(classOf[JUnitRunner])
class CounterSpec extends FunSpec with MockitoSugar with ShouldMatchers with OneInstancePerTest {
  describe("A counter") {
    val metric = mock[com.codahale.metrics.Counter]
    val counter = Counter(metric)

    it("should increment the underlying metric by an arbitrary amount") {
      counter += 12

      verify(metric).inc(12)
    }

    it("should decrement the underlying metric by an arbitrary amount") {
      counter -= 12

      verify(metric).dec(12)
    }
    
    it("should consult the underlying counter for current count") {
      counter += 1
      when(metric.getCount()).thenReturn(1L)
      
      counter.count should equal (1)
      verify(metric).getCount()
    }
  }
}