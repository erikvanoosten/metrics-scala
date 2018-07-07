package nl.grons.metrics4.scala

import com.codahale.metrics.MetricRegistry.MetricSupplier
import com.codahale.metrics.{Metric, MetricFilter}
import org.mockito.Matchers.same
import org.mockito.Mockito.when
import org.scalatest.FunSpec
import org.scalatest.Matchers._
import org.scalatest.mockito.MockitoSugar._

class ImplicitsSpec extends FunSpec {

  describe("Implicits") {
    it("brings the implicit conversion functionToMetricFilter into scope", LT_Scala212) {
      // sanity check:
      """val metricFilter: MetricFilter = (_: String, _: Metric) => true""" shouldNot compile
      """val metricSupplier: MetricSupplier[Metric] = () => new Metric {}""" shouldNot compile
      // actual test:
      """import Implicits._
         val metricFilter: MetricFilter = (_: String, _: Metric) => true""" should compile
      """import Implicits._
         val metricSupplier: MetricSupplier[Metric] = () => new Metric {}""" should compile
    }

    it("is not required in Scala 2.12 and later because of SAM support", GE_Scala212) {
      """val metricFilter: MetricFilter = (_: String, _: Metric) => true""" should compile
      """val metricSupplier: MetricSupplier[Metric] = () => new Metric {}""" should compile
    }

    it("still works in Scala 2.12 and later", GE_Scala212) {
      """import Implicits._
         val metricFilter: MetricFilter = (_: String, _: Metric) => true""" should compile
      """import Implicits._
         val metricSupplier: MetricSupplier[Metric] = () => new Metric {}""" should compile
    }
  }

  describe("Implicits.functionToMetricFilter") {
    it("creates a MetricFilter that passes arguments to the function and returns function result unchanged") {
      val f = mock[(String, Metric) => Boolean]
      val dummyName = "dummy"
      val dummyMetric = new Metric {}
      when(f.apply(same(dummyName), same(dummyMetric))).thenReturn(true, false)
      val metricFilter: MetricFilter = Implicits.functionToMetricFilter(f)
      metricFilter.matches(dummyName, dummyMetric) shouldBe true
      metricFilter.matches(dummyName, dummyMetric) shouldBe false
    }
  }

  describe("Implicits.functionToMetricSupplier") {
    it("creates a MetricSupplier that wraps the function unchanged") {
      val f = mock[() => Metric]
      val dummyMetric1 = new Metric {}
      val dummyMetric2 = new Metric {}
      when(f.apply()).thenReturn(dummyMetric1, dummyMetric2)
      val metricSupplier: MetricSupplier[Metric] = Implicits.functionToMetricSupplier(f)
      metricSupplier.newMetric() shouldBe theSameInstanceAs(dummyMetric1)
      metricSupplier.newMetric() shouldBe theSameInstanceAs(dummyMetric2)
    }
  }

}
