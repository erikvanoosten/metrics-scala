package nl.grons.metrics.scala

import org.mockito.Mockito._
import org.scalatest.OneInstancePerTest
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import akka.actor.Actor
import akka.actor.ActorSystem
import com.codahale.metrics.Timer.Context

object TestFixture {
  
  class Fixture extends MockitoSugar {
    val mockCounter = mock[Counter]
    val mockTimer = mock[Timer]
    val mockTimerContext = mock[Context]
    val mockMeter = mock[Meter]
    
    when(mockTimer.timerContext()).thenReturn(mockTimerContext)
  }
  
  trait MetricRegistryFixture extends MetricRegistry {
    val fixture: Fixture
    
    override def counter(name: String, maybeScope: Option[String] = None) = fixture.mockCounter
    
    override def timer(name: String, maybeScope: Option[String] = None) = fixture.mockTimer
    
    override def meter(name: String, maybeScope: Option[String] = None) = fixture.mockMeter
  }
  
  class TestActor(val fixture: Fixture) extends Actor with MetricRegistryFixture {
    
    val messages = new scala.collection.mutable.ListBuffer[String]()
    
    def receive = {
      case message: String => { 
        messages += message 
      }
    }
    
  }
  
  class ExceptionThrowingTestActor(val fixture: Fixture) extends Actor with MetricRegistryFixture {
    
	  def receive = {
	    case _ => throw new RuntimeException()
	  }
	  
  }
  
  
	class CounterTestActor(fixture: Fixture) extends TestActor(fixture) with ReceiveCounterActor
	
	class TimerTestActor(fixture: Fixture) extends TestActor(fixture) with ReceiveTimerActor
	
	class ExceptionMeterTestActor(fixture: Fixture) extends ExceptionThrowingTestActor(fixture) with ReceiveExceptionMeterActor
	
	class ComposedActor(fixture: Fixture) extends TestActor(fixture) 
		with ReceiveCounterActor with ReceiveTimerActor with ReceiveExceptionMeterActor 
}

@RunWith(classOf[JUnitRunner])
class ActorMetricsSpec extends FunSpec with MockitoSugar with ShouldMatchers with MetricRegistry {
  import TestFixture._
  import akka.testkit.TestActorRef
  import scala.concurrent.duration._
  import scala.concurrent.Await
  import akka.pattern.ask
  
  implicit val system = ActorSystem()
  
  describe("A counter actor") {
    it("increments counter on new messages") {
      val fixture = new Fixture
      val ref = TestActorRef(new CounterTestActor(fixture))
      ref ! "test"
      verify(fixture.mockCounter).+=(1)
      ref.underlyingActor.messages should contain ("test")
    }
  }
  
  describe("A timer actor") {
    it("times a message processing") {
      val fixture = new Fixture
      val ref = TestActorRef(new TimerTestActor(fixture))
      ref ! "test"
      verify(fixture.mockTimer).timerContext()
      verify(fixture.mockTimerContext).stop()
      ref.underlyingActor.messages should contain ("test")
    }
  }
  
  describe("A exception meter actor") {
    it("meters thrown exceptions") {
      val fixture = new Fixture
      val ref = TestActorRef(new ExceptionMeterTestActor(fixture))
      intercept[RuntimeException] { ref.receive("test") }
      verify(fixture.mockMeter).mark()
    }
  }
  
  describe("A composed actor") {
    it("counts and times processing of messages") {
      val fixture = new Fixture
      val ref = TestActorRef(new ComposedActor(fixture))
      ref ! "test"
      verify(fixture.mockCounter).+=(1)
      verify(fixture.mockTimer).timerContext()
      verify(fixture.mockTimerContext).stop()
      verify(fixture.mockMeter,never()).mark()
      ref.underlyingActor.messages should contain ("test")
    }
  }
  
}