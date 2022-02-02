/*
 * Copyright (c) 2013-2022 Erik van Oosten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.grons.metrics4.scala

import akka.actor.{Actor, ActorSystem}
import com.codahale.metrics.{Metric, MetricFilter, MetricRegistry}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.OneInstancePerTest

import scala.collection.JavaConverters._

class ActorMetricsSpec extends AnyFunSpec with OneInstancePerTest {
  import ActorMetricsSpec._
  import akka.testkit.TestActorRef

  implicit private val system: ActorSystem = ActorSystem()

  describe("A counter actor") {
    it("invokes original receive and increments counter on new messages") {
      val ref = TestActorRef(new CounterTestActor)
      val counterName = "nl.grons.metrics4.scala.ActorMetricsSpec.CounterTestActor.receiveCounter"

      counterValue(counterName) should be (0)

      ref.receive("test")

      ref.underlyingActor.messages should contain only "test"
      counterValue(counterName) should be (1)
    }

    it("can override metric name") {
      val ref = TestActorRef(new CounterTestActorNameOverride)
      val counterName = "nl.grons.metrics4.scala.ActorMetricsSpec.CounterTestActorNameOverride.overrideReceiveCounter"

      counterValue(counterName) should be (0)

      ref.receive("test")

      ref.underlyingActor.messages should contain only "test"
      counterValue(counterName) should be (1)
    }
  }

  describe("A timer actor") {
    it("invokes original receive and times message processing") {
      val ref = TestActorRef(new TimerTestActor)
      val timerName = "nl.grons.metrics4.scala.ActorMetricsSpec.TimerTestActor.receiveTimer"

      timerCountValue(timerName) should be (0)

      ref.receive("test")

      ref.underlyingActor.messages should contain only "test"
      timerCountValue(timerName) should be (1)
    }

    it("can override metric name") {
      val ref = TestActorRef(new TimerTestActor {
        override def receiveTimerName: String = "something-else"
      })
      val timerName = "nl.grons.metrics4.scala.ActorMetricsSpec.anon.something-else"

      timerCountValue(timerName) should be (0)

      ref.receive("test")

      ref.underlyingActor.messages should contain only "test"
      timerCountValue(timerName) should be (1)
    }
  }

  describe("A exception meter actor") {
    it("invokes original receive and meters thrown exceptions") {
      val ref = TestActorRef(new ExceptionMeterTestActor)
      val meterName = "nl.grons.metrics4.scala.ActorMetricsSpec.ExceptionMeterTestActor.receiveExceptionMeter"

      meterCountValue(meterName) should be (0)

      intercept[RuntimeException] { ref.receive("test") }

      ref.underlyingActor.messages should contain only "test"
      meterCountValue(meterName) should be (1)
    }
  }

  describe("A composed actor") {
    it("counts and times processing of messages") {
      val ref = TestActorRef(new ComposedActor)
      val counterName = "nl.grons.metrics4.scala.ActorMetricsSpec.ComposedActor.receiveCounter"
      val timerName = "nl.grons.metrics4.scala.ActorMetricsSpec.ComposedActor.receiveTimer"
      val meterName = "nl.grons.metrics4.scala.ActorMetricsSpec.ComposedActor.receiveExceptionMeter"

      counterValue(counterName) should be (0)
      timerCountValue(timerName) should be (0)
      meterCountValue(meterName) should be (0)

      intercept[RuntimeException] { ref.receive("test") }

      ref.underlyingActor.messages should contain only "test"
      counterValue(counterName) should be (1)
      timerCountValue(timerName) should be (1)
      meterCountValue(meterName) should be (1)
    }
  }

  private def counterValue(counterName: String): Long = {
    testMetricRegistry.getCounters(nameFilter(counterName)).values().asScala.headOption.map(_.getCount).getOrElse {
      fail(s"Counter '${counterName}' was not registered. Registered counters: " + testMetricRegistry.getCounters.keySet().asScala)
    }
  }

  private def timerCountValue(timerName: String): Long = {
    testMetricRegistry.getTimers(nameFilter(timerName)).values().asScala.headOption.map(_.getCount).getOrElse {
      fail(s"Timer '${timerName}' was not registered. Registered timers: " + testMetricRegistry.getTimers.keySet().asScala)
    }
  }

  private def meterCountValue(meterName: String): Long = {
    testMetricRegistry.getMeters(nameFilter(meterName)).values().asScala.headOption.map(_.getCount).getOrElse {
      fail(s"Meter '${meterName}' was not registered. Registered meters: " + testMetricRegistry.getMeters.keySet().asScala)
    }
  }

  private def nameFilter(filterName: String) = new MetricFilter {
    override def matches(name: String, metric: Metric): Boolean = name == filterName
  }
}

object ActorMetricsSpec {

  val testMetricRegistry = new MetricRegistry()

  trait ActorMetricsSpecInstrumented extends InstrumentedBuilder {
    val metricRegistry: MetricRegistry = testMetricRegistry
  }

  class TestActor extends Actor with ActorMetricsSpecInstrumented {
    val messages = new scala.collection.mutable.ListBuffer[String]()

    def receive: Actor.Receive = { case message: String => messages += message }
  }

  class ExceptionThrowingTestActor extends Actor with ActorMetricsSpecInstrumented {
    val messages = new scala.collection.mutable.ListBuffer[String]()

    private def storeMessage: Actor.Receive = { case message: String => messages += message }

    def receive: Actor.Receive = storeMessage.andThen({
      case _ => throw new RuntimeException()
    })
  }

  class CounterTestActor extends TestActor with ReceiveCounterActor

  class CounterTestActorNameOverride extends TestActor with ReceiveCounterActor {
    override def receiveCounterName = "overrideReceiveCounter"
  }

  class TimerTestActor extends TestActor with ReceiveTimerActor

  class ExceptionMeterTestActor extends ExceptionThrowingTestActor with ReceiveExceptionMeterActor

  class ComposedActor extends ExceptionThrowingTestActor
    with ReceiveCounterActor with ReceiveTimerActor with ReceiveExceptionMeterActor

}
