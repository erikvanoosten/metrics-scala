/*
 * Copyright (c) 2013-2021 Erik van Oosten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.grons.metrics4.scala

import com.codahale.metrics.MetricRegistry.MetricSupplier
import com.codahale.metrics.{Metric, MetricFilter}
import org.mockito.ArgumentMatchersSugar.same
import org.mockito.MockitoSugar._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._

class ImplicitsSpec extends AnyFunSpec {

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
