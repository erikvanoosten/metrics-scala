package nl.grons.metrics.scala

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.junit.Test
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.OneInstancePerTest

@RunWith(classOf[JUnitRunner])
class MeterSpec extends FunSpec with MockitoSugar with ShouldMatchers with OneInstancePerTest {
  val metric = mock[com.codahale.metrics.Meter]
  val meter = new Meter(metric)

  describe("A meter") {

    it("marks the underlying metric") {
      meter.mark()

      verify(metric).mark()
    }

    it("marks the underlying metric by an arbitrary amount") {
      meter.mark(12)

      verify(metric).mark(12)
    }
    
    it("increments meter on exception when exceptionMeter is used") {
      evaluating { meter.exceptionMeter( throw new RuntimeException() ) } should produce [RuntimeException]
      
      verify(metric).mark()
    }
  }
}