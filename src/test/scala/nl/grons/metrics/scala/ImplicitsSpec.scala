package nl.grons.metrics.scala

import com.codahale.metrics.{MetricFilter, Metric}
import org.scalatest.{FunSpec, Matchers}


class ImplicitsSpec extends FunSpec with Matchers {

  describe("Implicits.funToMetricFilter") {
    it("convert a (String, Metric) => Boolean function to a MetricFilter of the same semantics") {
      val emptyMetric = new Metric {}
      val fun = (name: String, _: Metric) => !name.contains("foo")
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

}
