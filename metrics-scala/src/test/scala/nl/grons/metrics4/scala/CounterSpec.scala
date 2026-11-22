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

import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._

class CounterSpec extends AnyFunSpec with MockFactory {
  describe("A counter") {

    it("+= should increment the underlying metric by an arbitrary amount") {
      withMockCounter { case (mockDwCounter, counter) =>
        counter += 10
        (mockDwCounter.inc(_: Long)).verify(10L).once()
      }
    }

    it("-= should decrement the underlying metric by an arbitrary amount") {
      withMockCounter { case (mockDwCounter, counter) =>
        counter -= 12
        (mockDwCounter.dec(_: Long)).verify(12L).once()
      }
    }

    it("inc should increment the underlying metric by an arbitrary amount") {
      withMockCounter { case (mockDwCounter, counter) =>
        counter.inc(14)
        (mockDwCounter.inc(_: Long)).verify(14L).once()
      }
    }

    it("dec should decrement the underlying metric by an arbitrary amount") {
      withMockCounter { case (mockDwCounter, counter) =>
        counter.dec(16)
        (mockDwCounter.dec(_: Long)).verify(16L).once()
      }
    }

    it("getCount should consult the underlying counter for current count") {
      withMockCounter { case (mockDwCounter, counter) =>
        (() => mockDwCounter.getCount).when().returns(1L)
        counter.count should equal (1)
        (() => mockDwCounter.getCount).verify().once()
      }
    }

    describe("count") {
      it("should consult the wrapper partial function for isDefined") {
        withWrappedPf { case (_, wrappedPf) =>
          wrappedPf.isDefinedAt("test") should be(true)
          wrappedPf.isDefinedAt("x") should be(false)
        }
      }

      it("should increment counter upon execution of partial function") {
        withWrappedPf { case (mockDwCounter, wrappedPf) =>
          wrappedPf("test") should equal("test")
          (mockDwCounter.inc(_: Long)).verify(1L).once()
        }
      }

      it("should increment counter upon execution of undefined partial function") {
        withWrappedPf { case (mockDwCounter, wrappedPf) =>
          a[MatchError] should be thrownBy wrappedPf("x")
          (mockDwCounter.inc(_: Long)).verify(1L).once()
        }
      }
    }

    it("countConcurrency should increment and decrement underlying counter upon execution of a function") {
      withMockCounter { case (mockDwCounter, counter) =>
        def dummyWork = 123
        val result = counter.countConcurrency(dummyWork)
        (mockDwCounter.inc(_: Long)).verify(1L).once()
        (mockDwCounter.dec(_: Long)).verify(1L).once()
        result should be (123)
      }
    }
  }

  private def withMockCounter[A](testCode: (TestCounter, Counter) => A): A = {
    val mockDwCounter = stub[TestCounter]
    val counter = new Counter(mockDwCounter)
    testCode(mockDwCounter, counter)
  }

  private def withWrappedPf[A](testCode: (TestCounter, PartialFunction[String, String]) => A): A = {
    withMockCounter { case (mockDwCounter, counter) =>
      val pf: PartialFunction[String, String] = { case "test" => "test" }
      val wrapped: PartialFunction[String, String] = counter.count(pf)
      testCode(mockDwCounter, wrapped)
    }
  }
}
