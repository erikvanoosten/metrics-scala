/*
 * Copyright (c) 2014-2026 Erik van Oosten
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

import com.codahale.metrics.MetricRegistry
import org.scalamock.scalatest.MockFactory
import org.scalatest.OneInstancePerTest
import org.scalatest.funspec.AnyFunSpec

class InstrumentedBuilderSpec extends AnyFunSpec with OneInstancePerTest with MockFactory {

  describe("InstrumentedBuilder") {
    it("uses owner class as metric base name") {
      val metricOwner = new MetricOwner
      metricOwner.createCounter()
      (metricOwner.metricRegistry.counter(_: String)).verify("nl.grons.metrics4.scala.InstrumentedBuilderSpec.MetricOwner.cnt").once()
    }

    it("supports overriding the metric base name") {
      val metricOwner = new MetricOwner {
        override lazy val metricBaseName: MetricName = MetricName("OverriddenBaseName")
      }
      metricOwner.createCounter()
      (metricOwner.metricRegistry.counter(_: String)).verify("OverriddenBaseName.cnt").once()
    }
  }

  private class MetricOwner() extends InstrumentedBuilder {
    val metricRegistry: MetricRegistry = stub[TestMetricRegistry]

    def createCounter(): Counter = metrics.counter("cnt")
  }

}
