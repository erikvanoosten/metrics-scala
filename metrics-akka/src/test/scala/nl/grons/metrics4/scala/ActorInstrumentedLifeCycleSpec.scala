/*
 * Copyright (c) 2013-2021 Erik van Oosten
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

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit._
import com.codahale.metrics._
import com.typesafe.config.ConfigFactory
import nl.grons.metrics4.scala.ActorInstrumentedLifeCycleSpec._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funspec.AsyncFunSpecLike
import org.scalatest.matchers.should.Matchers

import scala.collection.JavaConverters._
import scala.concurrent.Promise

object ActorInstrumentedLifeCycleSpec {

  // Don't log all those intentional exceptions
  val NonLoggingActorSystem = ActorSystem("lifecycle-spec", ConfigFactory.parseMap(Map("akka.loglevel" -> "OFF").asJava))

  val TestMetricRegistry = new com.codahale.metrics.MetricRegistry()

  trait Instrumented extends InstrumentedBuilder {
    val metricRegistry: MetricRegistry = TestMetricRegistry
  }

  class ExampleActor(restarted: Promise[Boolean]) extends Actor with Instrumented with ActorInstrumentedLifeCycle {

    var counter = 0

    // The following gauge will automatically unregister before a restart of this actor.
    metrics.gauge("sample-gauge") {
      counter
    }

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      self ! Symbol("prerestart")
      super.preRestart(reason, message)
    }

    def receive: Receive = {
      case Symbol("increment") =>
        counter += 1
      case Symbol("get") =>
        sender() ! counter
      case Symbol("error") =>
        throw new RuntimeException("BOOM!")
      case Symbol("prerestart") =>
        restarted.success(true)
    }

  }
}

class ActorInstrumentedLifeCycleSpec extends TestKit(NonLoggingActorSystem) with AsyncFunSpecLike with ImplicitSender with Matchers with ScalaFutures with BeforeAndAfterAll {

  def report: Option[Any] = {
    case class NameFilter(prefix: String) extends MetricFilter {
      override def matches(name: String, metric: Metric): Boolean = name.startsWith(prefix)
    }

    TestMetricRegistry
      .getGauges(NameFilter("nl.grons.metrics4.scala")).asScala
      .headOption
      .map { case (_, g) => g.getValue }
  }

  describe("an actor with gauges needing lifecycle management") {
    val restartedPromise = Promise[Boolean]()
    val ar = system.actorOf(Props(new ExampleActor(restartedPromise)))

    it("correctly builds a gauge that reports the correct value") {
      ar ! Symbol("increment")
      ar ! Symbol("increment")
      ar ! Symbol("increment")
      ar ! Symbol("get")
      expectMsg(3)
      report shouldBe Some(3)
    }

    it("rebuilds the gauge on actor restart") {
      ar ! Symbol("increment")
      ar ! Symbol("increment")
      ar ! Symbol("error")
      ar ! Symbol("increment")
      ar ! Symbol("get")
      expectMsg(1)
      whenReady(restartedPromise.future)(_ shouldBe true)
      report shouldBe Some(1)
    }
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
