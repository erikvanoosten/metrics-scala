package nl.grons.metrics.scala

import akka.actor.Actor

/**
 * Stackable actor trait - counts received messages
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
 *   def loadStuff(): Seq[Row] = loading.time {
 *     db.fetchRows()
 *   }
 * }
 * }}}
 */
trait ReceiveCounterActor extends Actor { self: InstrumentedBuilder =>

  def receiveCounterName: String = getClass.getName + ".receiveCounter"
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
 */
trait ReceiveTimerActor extends Actor { self: InstrumentedBuilder =>

  def receiveTimerName: String = getClass.getSuperclass.getName + ".receiveTimer"
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
 */
trait ReceiveExceptionMeterActor extends Actor { self: InstrumentedBuilder =>

  def receiveExceptionMeterName: String = getClass.getSuperclass.getName + ".receiveExceptionMeter"
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
