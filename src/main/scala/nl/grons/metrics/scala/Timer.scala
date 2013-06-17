package nl.grons.metrics.scala

import java.util.concurrent.TimeUnit
import com.codahale.metrics.{Timer => CHTimer}

object Timer {
  def apply(metric: CHTimer) = new Timer(metric)
  def unapply(metric: Timer) = Option(metric.metric)
  
  implicit def javaTimer2ScalaTimer(metric: CHTimer) = apply(metric)
  implicit def scalaTimer2JavaTimer(metric: Timer) = metric.metric
}

/**
 * A Scala façade class for Timer.
 */
class Timer(private val metric: CHTimer) {
  /**
   * Runs f, recording its duration, and returns the result of f.
   */
  def time[A](f: => A): A = {
    val ctx = metric.time
    try {
      f
    } finally {
      ctx.stop
    }
  }

  /**
   * Adds a recorded duration.
   */
  def update(duration: Long, unit: TimeUnit) {
    metric.update(duration, unit)
  }

  /**
   * Returns a timing [[com.metrics.yammer.core.TimerContext]],
   * which measures an elapsed time in nanoseconds.
   */
  def timerContext() = metric.time()

  /**
   * Returns the number of durations recorded.
   */
  def count = metric.getCount

  /**
   * Returns the longest recorded duration.
   */
  def max = snapshot.getMax

  /**
   * Returns the shortest recorded duration.
   */
  def min = snapshot.getMin

  /**
   * Returns the arithmetic mean of all recorded durations.
   */
  def mean = snapshot.getMean

  /**
   * Returns the standard deviation of all recorded durations.
   */
  def stdDev = snapshot.getStdDev

  /**
   * Returns a snapshot of the values in the timer's sample.
   */
  def snapshot = metric.getSnapshot

  /**
   * Returns the fifteen-minute rate of timings.
   */
  def fifteenMinuteRate = metric.getFifteenMinuteRate

  /**
   * Returns the five-minute rate of timings.
   */
  def fiveMinuteRate = metric.getFiveMinuteRate

  /**
   * Returns the mean rate of timings.
   */
  def meanRate = metric.getMeanRate

  /**
   * Returns the one-minute rate of timings.
   */
  def oneMinuteRate = metric.getOneMinuteRate
}

