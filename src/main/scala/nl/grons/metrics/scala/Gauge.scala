package nl.grons.metrics.scala

import com.codahale.metrics.{Gauge => CHGauge}

object Gauge {
    def apply[A](f: => A) = new Gauge[A]( new CHGauge[A] { def getValue = f } )
	def apply[A](metric: CHGauge[A]) = new Gauge[A](metric)
	def unapply[A](metric: Gauge[A]) = Option(metric.metric)
	
	def scalaGauge2CodahaleGauge[A](metric: Gauge[A]) = metric.metric
	def codahaleGauge2ScalaGauge[A](metric: CHGauge[A]) = new Gauge[A](metric)
}

class Gauge[T](private val metric: CHGauge[T]) {

  def value = metric.getValue()
  
}
