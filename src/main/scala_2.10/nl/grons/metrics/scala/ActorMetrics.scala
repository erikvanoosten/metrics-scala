package nl.grons.metrics.scala

import akka.actor.Actor
import com.codahale.metrics.MetricRegistry

/**
 * Stackable actor trait - counts received messages
 *
 * Metric name defaults to the class of the actor (e.g. ExampleActor below) + .receiveCounter
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
 * class ExampleActor extends ReceiveCounterActor with Instrumented {
 *
 *   def receive = {
 *     case _ => doWork()
 *   }
 * }
 * }}}
 */
trait ReceiveCounterActor extends Actor { self: InstrumentedBuilder =>

  def receiveCounterName: String = MetricRegistry.name(getClass,"receiveCounter")
  lazy val counter: Counter = metrics.counter(receiveCounterName)

  abstract override def receive = {
    case msg => {
      counter += 1
      super.receive(msg)
    }
  }

}

/**
 * Stackable actor trait - times the message receipt
 *
 * Metric name defaults to the class of the actor (e.g. ExampleActor below) + .receiveTimer
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
 * class ExampleActor extends ReceiveTimerActor with Instrumented {
 *
 *   def receive = {
 *     case _ => doWork()
 *   }
 * }
 * }}}
 */
trait ReceiveTimerActor extends Actor { self: InstrumentedBuilder =>

  def receiveTimerName: String = MetricRegistry.name(getClass,"receiveTimer")
  lazy val timer: Timer = metrics.timer(receiveTimerName)

  abstract override def receive = {
    case msg => {
      val ctx = timer.timerContext()
      try {
        super.receive(msg)
      } finally {
        ctx.stop()
      }
    }
  }
}

/**
 * Stackable actor trait - meters the exceptions thrown
 *
 * Metric name defaults to the class of the actor (e.g. ExampleActor below) + .receiveExceptionMeter
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
 * class ExampleActor extends ReceiveTimerActor with Instrumented {
 *
 *   def receive = {
 *     case _ => doWork()
 *   }
 * }
 * }}}
 */
trait ReceiveExceptionMeterActor extends Actor { self: InstrumentedBuilder =>

  def receiveExceptionMeterName: String = MetricRegistry.name(getClass,"receiveExceptionMeter")
  lazy val meter: Meter = metrics.meter(receiveExceptionMeterName)

  abstract override def receive = {
    case msg => {
      try {
        super.receive(msg)
      } catch {
        case e: Throwable => {
          meter.mark()
          throw e
        }
      }
    }
  }

}
