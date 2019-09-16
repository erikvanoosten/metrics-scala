/*
 * Copyright (c) 2013-2019 Erik van Oosten
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

import org.mockito.IdiomaticMockito._
import matchers.should.Matchers._
import org.scalatest.OneInstancePerTest
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers

class MeterSpec extends AnyFunSpec with OneInstancePerTest {
  describe("A meter") {
    val metric = mock[com.codahale.metrics.Meter]
    val meter = new Meter(metric)

    it("marks the underlying metric") {
      meter.mark()

      metric.mark() was called
    }

    it("marks the underlying metric by an arbitrary amount") {
      meter.mark(12)

      metric.mark(12) was called
    }

    it("increments meter on exception when exceptionMeter is used") {
      a [RuntimeException] should be thrownBy { meter.exceptionMarker( throw new RuntimeException() ) }

      metric.mark() was called
    }

    it("should increment time execution of partial function") {
      val pf: PartialFunction[String,String] = { case "test" => throw new RuntimeException() }
      val wrapped = meter.exceptionMarkerPF(pf)
      a [RuntimeException] should be thrownBy { wrapped("test") }
      metric.mark() was called
      wrapped.isDefinedAt("x") should be (false)
    }
  }
}