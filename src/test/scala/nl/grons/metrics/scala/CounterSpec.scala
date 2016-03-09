/*
 * Copyright (c) 2013-2016 Erik van Oosten
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

import org.mockito.Mockito.{when, verify}
import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers._
import org.scalatest.OneInstancePerTest

@RunWith(classOf[JUnitRunner])
class CounterSpec extends FunSpec with OneInstancePerTest {
  describe("A counter") {
    val metric = mock[com.codahale.metrics.Counter]
    val counter = new Counter(metric)

    it("+= should increment the underlying metric by an arbitrary amount") {
      counter += 12
      verify(metric).inc(12)
    }

    it("-= should decrement the underlying metric by an arbitrary amount") {
      counter -= 12
      verify(metric).dec(12)
    }

    it("inc should increment the underlying metric by an arbitrary amount") {
      counter.inc(12)
      verify(metric).inc(12)
    }

    it("dec should decrement the underlying metric by an arbitrary amount") {
      counter.dec(12)
      verify(metric).dec(12)
    }

    it("getCount should consult the underlying counter for current count") {
      when(metric.getCount).thenReturn(1L)
      counter.count should equal (1)
      verify(metric).getCount
    }

    describe("count") {
      val pf: PartialFunction[String, String] = { case "test" => "test" }
      val wrapped = counter.count(pf)

      it("should consult the wrapper partial function for isDefined") {
        wrapped.isDefinedAt("test") should be(true)
        wrapped.isDefinedAt("x") should be(false)
      }

      it("should increment counter upon execution of partial function") {
        wrapped("test") should equal("test")
        verify(metric).inc(1)
      }

      it("should increment counter upon execution of undefined partial function") {
        a[MatchError] should be thrownBy wrapped("x")
        verify(metric).inc(1)
      }
    }

    it("countConcurrency should increment and decrement underlying counter upon execution of a function") {
      def dummyWork = 123
      val result = counter.countConcurrency(dummyWork)
      verify(metric).inc(1)
      verify(metric).dec(1)
      result should be (123)
    }
  }
}