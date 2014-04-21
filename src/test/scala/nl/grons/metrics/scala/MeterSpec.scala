/*
 * Copyright (c) 2013-2014 Erik van Oosten
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

package nl.grons.metrics.scala

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.OneInstancePerTest

@RunWith(classOf[JUnitRunner])
class MeterSpec extends FunSpec with MockitoSugar with ShouldMatchers with OneInstancePerTest {
  describe("A meter") {
    val metric = mock[com.codahale.metrics.Meter]
    val meter = new Meter(metric)

    it("marks the underlying metric") {
      meter.mark()

      verify(metric).mark()
    }

    it("marks the underlying metric by an arbitrary amount") {
      meter.mark(12)

      verify(metric).mark(12)
    }

    it("increments meter on exception when exceptionMeter is used") {
      evaluating { meter.exceptionMarker( throw new RuntimeException() ) } should produce [RuntimeException]

      verify(metric).mark()
    }

    it("should increment time execution of partial function") {
      val pf: PartialFunction[String,String] = { case "test" => throw new RuntimeException() }
      val wrapped = meter.exceptionMarkerPF(pf)
      evaluating { wrapped("test") } should produce [RuntimeException]
      verify(metric).mark()
      wrapped.isDefinedAt("x") should be (false)
    }
  }
}