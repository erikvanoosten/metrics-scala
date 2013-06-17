package nl.grons.metrics.scala

import org.mockito.Mockito._
import org.scalatest.OneInstancePerTest
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.util.concurrent.TimeUnit

@RunWith(classOf[JUnitRunner])
class TimerSpec extends FunSpec with MockitoSugar with ShouldMatchers with OneInstancePerTest {
  describe("A timer") {
    val metric = mock[com.codahale.metrics.Timer]
    val timer = new Timer(metric)
    val context = mock[com.codahale.metrics.Timer.Context]
    when(metric.time()).thenReturn(context)

    it("times the passed closure") {
      timer.time { 1 }

      verify(metric).time()
      verify(context).stop()
    }

    it("updates the underlying metric") {
      timer.update(1L,TimeUnit.MILLISECONDS)

      verify(metric).update(1L,TimeUnit.MILLISECONDS)
    }
  }
}