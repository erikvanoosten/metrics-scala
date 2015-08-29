package nl.grons.metrics.scala

import com.codahale.metrics.{MetricFilter, Metric}

import scala.language.implicitConversions

object Implicits {
  implicit def funToMetricFilter(filterFun: (String, Metric) => Boolean): MetricFilter = new MetricFilter {
    override def matches(name: String, metric: Metric) = filterFun(name, metric)
  }
}
