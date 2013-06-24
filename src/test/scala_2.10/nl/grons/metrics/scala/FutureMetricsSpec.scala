

package nl.grons.metrics.scala

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.OneInstancePerTest
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.FunSpec
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import com.codahale.metrics.Timer.Context
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

@RunWith(classOf[JUnitRunner])
class FutureMetricsSpec extends FunSpec with MockitoSugar with ShouldMatchers with OneInstancePerTest  
		with ConcurrentMetrics with MetricRegistry {

  var timeCalled = false
  val mockTimer = new Timer(null) { 
    override def time[A](action: =>A) = { timeCalled = true; action }
    override def timerContext = mockTimerContext
  }
  val mockTimerContext = mock[Context]
  
  override def timer(name: String, maybeScope: Option[String] = None) = {
    mockTimer
  }
  
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