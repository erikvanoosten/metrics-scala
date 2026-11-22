/*
 * Copyright (c) 2026 Erik van Oosten
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

package com.codahale.metrics {
  class TestTimerContext extends Timer.Context(new Timer(), Clock.defaultClock())
}

package nl.grons.metrics4.scala {

  import com.codahale.metrics._
  import com.codahale.metrics.health.HealthCheckRegistry

  class TestMetricRegistry extends MetricRegistry {
    final override def buildMap(): java.util.concurrent.ConcurrentMap[String, Metric] =
      new java.util.concurrent.ConcurrentHashMap[String, Metric]()

    override def getGauges(): java.util.SortedMap[String, Gauge[_]] =
      super.getGauges()

    override def getGauges(filter: MetricFilter): java.util.SortedMap[String, Gauge[_]] =
      super.getGauges(filter)
  }

  class TestHealthCheckRegistry extends HealthCheckRegistry
  class TestCounter extends Counter
  class TestHistogram extends Histogram(new UniformReservoir())
  class TestSnapshot extends Snapshot {
    def getValue(quantile: Double): Double = 0.0
    def getValues(): Array[Long] = Array.empty
    def size(): Int = 0
    def getMax(): Long = 0L
    def getMean(): Double = 0.0
    def getMin(): Long = 0L
    def getStdDev(): Double = 0.0
    def dump(outputStream: java.io.OutputStream): Unit = ()
  }
  class TestMeter extends Meter
  class TestTimer extends Timer
}
