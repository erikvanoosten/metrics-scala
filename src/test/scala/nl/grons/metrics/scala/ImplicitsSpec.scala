package nl.grons.metrics.scala

import com.codahale.metrics.{MetricFilter, Metric}
import org.scalatest.{FunSpec, Matchers}


class ImplicitsSpec extends FunSpec with Matchers {

  describe("Implicits.funToMetricFilter") {

    it("converts a (String, Metric) => Boolean function to a MetricFilter of the same semantics") {
      val fun = (name: String, _: Metric) => !name.contains("foo")
      val emptyMetric = new Metric {}
      val filter = new MetricFilter {
        override def matches(name: String, metric: Metric) = !name.contains("foo")
      }

      val filterFromFun = Implicits.funToMetricFilter(fun)

      Seq(("foo", false), ("foo.bar", false), ("bar", true)).foreach { case (name, expectedResult) =>
        val matchResult = filterFromFun.matches(name, emptyMetric)
        matchResult shouldEqual expectedResult
        filter.matches(name, emptyMetric) shouldEqual filterFromFun.matches(name, emptyMetric)
      }
    }
  }

  describe("Implicits") {
    it("should bring a implicit conversions from (String, Metric) => Boolean to MetricFilter into scope") {
      import Implicits._
      def filterFun(name: String, metric: Metric) = false
      val filter: MetricFilter = filterFun _
      filter shouldBe a[MetricFilter]
    }
  }

}
