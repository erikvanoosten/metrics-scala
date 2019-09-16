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

class CounterSpec extends AnyFunSpec with OneInstancePerTest {
  describe("A counter") {
    val metric = mock[com.codahale.metrics.Counter]
    val counter = new Counter(metric)

    it("+= should increment the underlying metric by an arbitrary amount") {
      counter += 12
      metric.inc(12) was called
    }

    it("-= should decrement the underlying metric by an arbitrary amount") {
      counter -= 12
      metric.dec(12) was called
    }

    it("inc should increment the underlying metric by an arbitrary amount") {
      counter.inc(12)
      metric.inc(12) was called
    }

    it("dec should decrement the underlying metric by an arbitrary amount") {
      counter.dec(12)
      metric.dec(12) was called
    }

    it("getCount should consult the underlying counter for current count") {
      metric.getCount shouldReturn 1L
      counter.count should equal (1)
      metric.getCount was called
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
        metric.inc(1) was called
      }

      it("should increment counter upon execution of undefined partial function") {
        a[MatchError] should be thrownBy wrapped("x")
        metric.inc(1) was called
      }
    }

    it("countConcurrency should increment and decrement underlying counter upon execution of a function") {
      def dummyWork = 123
      val result = counter.countConcurrency(dummyWork)
      metric.inc(1) was called
      metric.dec(1) was called
      result should be (123)
    }
  }
}