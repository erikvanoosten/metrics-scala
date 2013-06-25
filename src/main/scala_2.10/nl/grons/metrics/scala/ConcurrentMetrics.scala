package nl.grons.metrics.scala

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import akka.actor.Actor

/**
 * Supplies name for actor metrics
 */
trait NamedMetric {
  def metricName: String = getClass.getSuperclass.getName
}

/**
 * Stackable actor trait - counts received messages
 */
trait ReceiveCounterActor extends Actor with NamedMetric { self: MetricRegistry =>

  val counter: Counter = counter(metricName)

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
trait ReceiveTimerActor extends Actor with NamedMetric { self: MetricRegistry =>

  val timer: Timer = timer(metricName)

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
trait ReceiveExceptionMeterActor extends Actor with NamedMetric { self: MetricRegistry =>
  val meter: Meter = meter(metricName)

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

/**
 * Provides timing of future execution
 */
trait ConcurrentMetrics { self: MetricRegistry =>
  def timed[A](metricName: String, action: => A)(implicit context: ExecutionContext): Future[A] = {
    val timer = self.timer(metricName)
    Future(timer.time(action))
  }

}