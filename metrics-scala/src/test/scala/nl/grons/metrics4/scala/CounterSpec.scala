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

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._

class CounterSpec extends AnyFunSpec {
  describe("A counter") {

    it("+= should increment the underlying metric by an arbitrary amount") {
      withMockCounter { case (mockDwCounter, counter) =>
        counter += 10
        verify(mockDwCounter).inc(10)
      }
    }

    it("-= should decrement the underlying metric by an arbitrary amount") {
      withMockCounter { case (mockDwCounter, counter) =>
        counter -= 12
        verify(mockDwCounter).dec(12)
      }
    }

    it("inc should increment the underlying metric by an arbitrary amount") {
      withMockCounter { case (mockDwCounter, counter) =>
        counter.inc(14)
        verify(mockDwCounter).inc(14)
      }
    }

    it("dec should decrement the underlying metric by an arbitrary amount") {
      withMockCounter { case (mockDwCounter, counter) =>
        counter.dec(16)
        verify(mockDwCounter).dec(16)
      }
    }

    it("getCount should consult the underlying counter for current count") {
      withMockCounter { case (mockDwCounter, counter) =>
        when(mockDwCounter.getCount).thenReturn(1L)
        counter.count should equal (1)
        verify(mockDwCounter).getCount
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
          verify(mockDwCounter).inc(1)
        }
      }

      it("should increment counter upon execution of undefined partial function") {
        withWrappedPf { case (mockDwCounter, wrappedPf) =>
          a[MatchError] should be thrownBy wrappedPf("x")
          verify(mockDwCounter).inc(1)
        }
      }
    }

    it("countConcurrency should increment and decrement underlying counter upon execution of a function") {
      withMockCounter { case (mockDwCounter, counter) =>
        def dummyWork = 123
        val result = counter.countConcurrency(dummyWork)
        verify(mockDwCounter).inc(1)
        verify(mockDwCounter).dec(1)
        result should be (123)
      }
    }
  }

  private def withMockCounter[A](testCode: (com.codahale.metrics.Counter, Counter) => A): A = {
    val mockDwCounter = mock(classOf[com.codahale.metrics.Counter])
    val counter = new Counter(mockDwCounter)
    testCode(mockDwCounter, counter)
  }

  private def withWrappedPf[A](testCode: (com.codahale.metrics.Counter, PartialFunction[String, String]) => A): A = {
    withMockCounter { case (mockDwCounter, counter) =>
      val pf: PartialFunction[String, String] = { case "test" => "test" }
      val wrapped: PartialFunction[String, String] = counter.count(pf)
      testCode(mockDwCounter, wrapped)
    }
  }
}