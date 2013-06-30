package nl.grons.metrics.scala

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

/**
 * Provides timing of future execution
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
 * class Example(db: Database) extends Instrumented with FutureMetrics {
 *   import scala.concurrent._
 *   import ExecutionContext.Implicits.global
 *
 *   def loadStuffEventually(): Future[Seq[Row]] = timed("loading") {
 *     db.fetchRows()
 *   }
 * }
 * }}}
 */
trait FutureMetrics { self: InstrumentedBuilder =>
  def timed[A](metricName: String)(action: => A)(implicit context: ExecutionContext): Future[A] = {
    val timer = metrics.timer(metricName)
    Future(timer.time(action))
  }

}