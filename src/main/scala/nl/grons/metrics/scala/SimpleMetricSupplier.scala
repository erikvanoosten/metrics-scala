package nl.grons.metrics.scala

import com.codahale.metrics.Metric
import com.codahale.metrics.MetricRegistry.MetricSupplier

class SimpleMetricSupplier[M <: Metric](supplierFn: () ⇒ M) extends MetricSupplier[M] {
  override def newMetric(): M = supplierFn()
}

object SimpleMetricSupplier {
  def apply[M <: Metric](supplierFn: ⇒ M) =
    new SimpleMetricSupplier[M](() ⇒ supplierFn)
}

