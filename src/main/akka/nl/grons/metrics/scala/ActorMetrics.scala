/*
 * Copyright (c) 2013-2015 Erik van Oosten
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

package nl.grons.metrics.scala

import akka.actor.Actor

/**
 * Stackable actor trait which counts received messages.
 *
 * Metric name defaults to the class of the actor (e.g. `ExampleActor` below) + .`receiveCounter`
 *
 * Use it as follows:
 * {{{
 * object Application {
 *   // The application wide metrics registry.
 *   val metricRegistry = new com.codahale.metrics.MetricRegistry()
 * }
 * trait Instrumented extends InstrumentedBuilder {
 *   val metricRegistry = Application.metricRegistry
 * }
 *
 * class ExampleActor extends Actor {
 *
 *   def receive = {
 *     case _ => doWork()
 *   }
 * }
 *
 * class InstrumentedExampleActor extends ExampleActor with ReceiveCounterActor with Instrumented
 *
 * }}}
 */
trait ReceiveCounterActor extends Actor { self: InstrumentedBuilder =>

  def receiveCounterName: String = MetricName(getClass).append("receiveCounter").name
  lazy val counter: Counter = metrics.counter(receiveCounterName)

  private[this] lazy val wrapped = counter.count(super.receive)

  abstract override def receive = wrapped

}

/**
 * Stackable actor trait which times the message receipt.
 *
 * Metric name defaults to the class of the actor (e.g. `ExampleActor` below) + `.receiveTimer`
 *
 * Use it as follows:
 * {{{
 * object Application {
 *   // The application wide metrics registry.
 *   val metricRegistry = new com.codahale.metrics.MetricRegistry()
 * }
 * trait Instrumented extends InstrumentedBuilder {
 *   val metricRegistry = Application.metricRegistry
 * }
 *
 * class ExampleActor extends Actor {
 *
 *   def receive = {
 *     case _ => doWork()
 *   }
 * }
 *
 * class InstrumentedExampleActor extends ExampleActor with ReceiveCounterActor with Instrumented
 *
 * }}}
 */
trait ReceiveTimerActor extends Actor { self: InstrumentedBuilder =>

  def receiveTimerName: String = MetricName(getClass).append("receiveTimer").name
  lazy val timer: Timer = metrics.timer(receiveTimerName)

  private[this] lazy val wrapped = timer.timePF(super.receive)

  abstract override def receive = wrapped
}

/**
 * Stackable actor trait which meters thrown exceptions.
 *
 * Metric name defaults to the class of the actor (e.g. `ExampleActor` below) + `.receiveExceptionMeter`
 *
 * Use it as follows:
 * {{{
 * object Application {
 *   // The application wide metrics registry.
 *   val metricRegistry = new com.codahale.metrics.MetricRegistry()
 * }
 * trait Instrumented extends InstrumentedBuilder {
 *   val metricRegistry = Application.metricRegistry
 * }
 *
 * class ExampleActor extends Actor {
 *
 *   def receive = {
 *     case _ => doWork()
 *   }
 * }
 *
 * class InstrumentedExampleActor extends ExampleActor with ReceiveCounterActor with Instrumented
 *
 * }}}
 */
trait ReceiveExceptionMeterActor extends Actor { self: InstrumentedBuilder =>

  def receiveExceptionMeterName: String = MetricName(getClass).append("receiveExceptionMeter").name
  lazy val meter: Meter = metrics.meter(receiveExceptionMeterName)

  private[this] lazy val wrapped = meter.exceptionMarkerPF(super.receive)

  abstract override def receive = wrapped

}

/**
  * Actor helper trait which links the lifecycle of gauge registration/removal to the actor start/stop lifecycle.
  *
  * All gauges are removed by class name on restart due to exception.
  *
  * Expects default metric naming scheme of <class>.<metric_name>
  *
  * Use it as follows:
  * {{{
  * object Application {
  *   // The application wide metrics registry.
  *   val metricRegistry = new com.codahale.metrics.MetricRegistry()
  * }
  * trait Instrumented extends InstrumentedBuilder {
  *   val metricRegistry = Application.metricRegistry
  * }
  *
  * class ExampleActor extends Actor with Instrumented with ActorLifecycleMetricsLink {
  *
  *   var counter = 0
  *
  *   metrics.gauge("counter"){
  *     counter
  *   }
  *
  *   override def receive = {
  *     case 'increment =>
  *       counter += 1
  *       doWork()
  *   }
  *
  *   def doWork(): Unit = {
  *     // etc etc etc
  *   }
  * }
  *
  * }}}
  */
trait ActorLifecycleMetricsLink extends Actor with GaugeCleanup { self: InstrumentedBuilder =>

  override def preRestart(reason: Throwable, message: Option[Any]) = {
    cleanupByPrefix(getClass.getName)
    super.preRestart(reason,message)
  }

}