/*
 * Copyright (c) 2013-2022 Erik van Oosten
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

import com.codahale.metrics.SharedMetricRegistries
import com.codahale.metrics.health.SharedHealthCheckRegistries
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._

class FreshRegistriesSpec extends AnyFunSpec {

  describe(classOf[FreshRegistries].getSimpleName) {
    it("overrides the metric registry") {
      val metricOwner = new MetricOwner() with FreshRegistries

      metricOwner.metricRegistry should not be SharedMetricRegistries.getOrCreate("default")
      metricOwner.registry should not be SharedHealthCheckRegistries.getOrCreate("default")

      noException should be thrownBy {
        new MetricOwner() with FreshRegistries
      }
    }

    it("overrides the health check registry") {
      val healthCheckOwner = new HealthCheckOwner() with FreshRegistries

      healthCheckOwner.metricRegistry should not be SharedMetricRegistries.getOrCreate("default")
      healthCheckOwner.registry should not be SharedHealthCheckRegistries.getOrCreate("default")
    }
  }

  describe(classOf[FreshMetricRegistry].getSimpleName) {
    it("overrides the metric registry") {
      val metricOwner = new MetricOwner() with FreshMetricRegistry

      metricOwner.metricRegistry should not be SharedMetricRegistries.getOrCreate("default")
      metricOwner.registry shouldBe SharedHealthCheckRegistries.getOrCreate("default")

      noException should be thrownBy {
        new MetricOwner() with FreshMetricRegistry
      }
    }
  }

  describe(classOf[FreshHealthCheckRegistry].getSimpleName) {
    it("overrides the health check registry") {
      val healthCheckOwner = new HealthCheckOwner() with FreshHealthCheckRegistry

      healthCheckOwner.metricRegistry shouldBe SharedMetricRegistries.getOrCreate("default")
      healthCheckOwner.registry should not be SharedHealthCheckRegistries.getOrCreate("default")
    }
  }

  private class MetricOwner() extends DefaultInstrumented {
    metrics.gauge("aGauge") { 1 }
  }

  private class HealthCheckOwner() extends DefaultInstrumented {
    healthCheck("aHealthCheck") { true }
  }

}