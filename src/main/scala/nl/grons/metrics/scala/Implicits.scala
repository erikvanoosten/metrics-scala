package nl.grons.metrics.scala

import com.codahale.metrics.{Metric, MetricFilter}

import scala.language.implicitConversions

object Implicits {
  /**
   * Creates a [[MetricFilter]] from a regular Scala function that accepts a name and a metric and
   * returns a boolean: `(String, Metric) => Boolean`.
   *
   * @param f the function to convert
   * @return a [[MetricFilter]]
   */
  implicit def functionToMetricFilter(f: (String, Metric) => Boolean): MetricFilter = new MetricFilter {
    override def matches(name: String, metric: Metric) = f(name, metric)
  }
}
