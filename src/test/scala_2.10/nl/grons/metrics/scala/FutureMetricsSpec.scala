

package nl.grons.metrics.scala

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.OneInstancePerTest
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.FunSpec
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.{Timer => CHTimer}
import com.codahale.metrics.Timer.Context

@RunWith(classOf[JUnitRunner])
class FutureMetricsSpec extends FunSpec with ShouldMatchers with OneInstancePerTest  
		with FutureMetrics with InstrumentedBuilder {
  
  import MockitoSugar._
  import Matchers._
  
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
  
  implicit def ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor)
  
  describe("A future timer") {
    it("should time an execution") {
      val action = () => { Thread.sleep(100L) }
      val f = timed("test", action)
      val result = Await.result(f, Duration(300L,TimeUnit.MILLISECONDS))
      timeCalled should be (true)
    }
  } 
  
}