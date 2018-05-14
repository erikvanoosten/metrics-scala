/*
 * Copyright (c) 2013-2018 Erik van Oosten
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

package nl.grons.metrics5.scala

import akka.actor.Actor

/**
 * Stackable [[Actor]] trait which links [[Gauge]] life cycles with the actor life cycle.
 *
 * When an actor is restarted, gauges can not be created again under the same name in the same metric registry.
 * By mixing in this trait, all gauges created in this actor will be automatically unregistered before this actor
 * restarts.
 *
 * Use it as follows:
 * {{{
 * object Application {
 *   // The application wide metrics registry.
 *   val metricRegistry = new io.dropwizard.metrics5.MetricRegistry()
 * }
 * trait Instrumented extends InstrumentedBuilder {
 *   val metricRegistry = Application.metricRegistry
 * }
 *
 * class ExampleActor extends Actor with Instrumented with ActorInstrumentedLifecycle {
 *
 *   var counter = 0
 *
 *   // The following gauge will automatically unregister before a restart of this actor.
 *   metrics.gauge("sample-gauge") {
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
trait ActorInstrumentedLifeCycle extends Actor { self: InstrumentedBuilder =>

  abstract override def preRestart(reason: Throwable, message: Option[Any]) = {
    metrics.unregisterGauges()
    super.preRestart(reason, message)
  }

}

/**
 * Stackable actor trait which counts received messages.
 *
 * Metric name defaults to the class of the actor (e.g. `ExampleActor` below) + .`receiveCounter`
 *
 * Use it as follows:
 * {{{
 * object Application {
 *   // The application wide metrics registry.
 *   val metricRegistry = new io.dropwizard.metrics5.MetricRegistry()
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

  def receiveCounterName: String = "receiveCounter"
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
 *   val metricRegistry = new io.dropwizard.metrics5.MetricRegistry()
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
 * class InstrumentedExampleActor extends ExampleActor with ReceiveTimerActor with Instrumented
 *
 * }}}
 */
trait ReceiveTimerActor extends Actor { self: InstrumentedBuilder =>

  def receiveTimerName: String = "receiveTimer"
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
 *   val metricRegistry = new io.dropwizard.metrics5.MetricRegistry()
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
 * class InstrumentedExampleActor extends ExampleActor with ReceiveExceptionMeterActor with Instrumented
 *
 * }}}
 */
trait ReceiveExceptionMeterActor extends Actor { self: InstrumentedBuilder =>

  def receiveExceptionMeterName: String = "receiveExceptionMeter"
  lazy val meter: Meter = metrics.meter(receiveExceptionMeterName)

  private[this] lazy val wrapped = meter.exceptionMarkerPF(super.receive)

  abstract override def receive = wrapped

}
