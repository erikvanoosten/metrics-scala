package nl.grons.metrics.scala

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit._
import com.codahale.metrics._
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSpecLike, Matchers}

import scala.concurrent.Promise

object Application {
  // The application wide metrics registry.
  val metricRegistry = new com.codahale.metrics.MetricRegistry()
}

trait Instrumented extends InstrumentedBuilder {
  val metricRegistry = Application.metricRegistry
}

class ExampleActor(restarted: Promise[Boolean]) extends Actor with Instrumented with ActorLifecycleMetricsLink {

  var counter = 0

  metrics.gauge("counter"){
    counter
  }

  override def preRestart(reason: Throwable, message: Option[Any]) = {
    self ! 'prerestart
    super.preRestart(reason, message)
  }

  def receive = {
    case 'increment =>
      counter += 1
    case 'get =>
      sender() ! counter
    case 'error =>
      throw new RuntimeException("BOOM!")
    case 'prerestart =>
      restarted.success(true)
  }

}

@RunWith(classOf[JUnitRunner])
class ActorGaugeLifecycleSpec extends TestKit(ActorSystem("lifecycle_spec")) with FunSpecLike with ImplicitSender with Matchers with ScalaFutures with BeforeAndAfterAll {
  import collection.JavaConverters._

  case class NameFilter(prefix: String) extends MetricFilter {
    override def matches(name: String, metric: Metric): Boolean = name.startsWith(prefix)
  }

  def report = {
    Application.metricRegistry
      .getGauges(NameFilter("nl.grons.metrics.scala")).asScala
      .headOption
      .map{case (_,g) => g.getValue}
  }

  describe("an actor with gauges needing lifecycle management") {
    val restartedPromise = Promise[Boolean]()
    val ar = system.actorOf(Props(new ExampleActor(restartedPromise)))
    it("correctly builds a gauge that reports the correct value") {
      ar ! 'increment
      ar ! 'increment
      ar ! 'increment
      ar ! 'get
      expectMsg(3)
      report shouldBe Some(3)
    }

    it("rebuilds the gauge on actor restart") {
      ar ! 'error
      ar ! 'increment
      ar ! 'get
      expectMsg(1)
      whenReady(restartedPromise.future)(_ shouldBe true)
      report shouldBe Some(1)
    }
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
