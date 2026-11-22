/*
 * Copyright (c) 2013-2026 Erik van Oosten
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
import com.codahale.metrics.health.{HealthCheck, HealthCheckRegistry}
import org.scalamock.scalatest.MockFactory
import org.scalatest.OneInstancePerTest
import org.scalatest.funspec.AnyFunSpec

class CombinedBuilderSpec extends AnyFunSpec with OneInstancePerTest with MockFactory {

  describe("InstrumentedBuilder combined with CheckedBuilder") {
    it("uses owner class as metric base name") {
      val combinedBuilder = new CombinedBuilder

      combinedBuilder.createCounter()
      val check = combinedBuilder.createBooleanHealthCheck { true }

      (combinedBuilder.metricRegistry.counter(_: String)).verify("nl.grons.metrics4.scala.CombinedBuilderSpec.CombinedBuilder.cnt").once()
      (combinedBuilder.registry.register _).verify("nl.grons.metrics4.scala.CombinedBuilderSpec.CombinedBuilder.test", check).once()
    }

    it("supports overriding the metric base name") {
      val combinedBuilder = new CombinedBuilder {
        override lazy val metricBaseName: MetricName = MetricName("OverriddenBaseName")
      }

      combinedBuilder.createCounter()
      val check = combinedBuilder.createBooleanHealthCheck { true }

      (combinedBuilder.metricRegistry.counter(_: String)).verify("OverriddenBaseName.cnt").once()
      (combinedBuilder.registry.register _).verify("OverriddenBaseName.test", check).once()
    }
  }

  private class CombinedBuilder() extends InstrumentedBuilder with CheckedBuilder {
    val metricRegistry: MetricRegistry = stub[TestMetricRegistry]
    val registry: HealthCheckRegistry = stub[TestHealthCheckRegistry]

    def createCounter(): Counter = metrics.counter("cnt")

    def createBooleanHealthCheck(checker: Boolean): HealthCheck =
      healthCheck("test", "FAIL") { checker }
  }

}
