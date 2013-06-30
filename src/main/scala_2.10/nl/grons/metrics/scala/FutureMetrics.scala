package nl.grons.metrics.scala

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

/**
 * Provides timing of future execution
 */
trait FutureMetrics { self: InstrumentedBuilder =>
  def timed[A](metricName: String, action: => A)(implicit context: ExecutionContext): Future[A] = {
    val timer = metrics.timer(metricName)
    Future(timer.time(action))
  }

}