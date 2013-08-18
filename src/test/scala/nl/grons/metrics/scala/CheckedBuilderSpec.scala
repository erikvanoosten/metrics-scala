/*
 * Copyright (c) 2013-2013 Erik van Oosten
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

import com.codahale.metrics.health.HealthCheck.Result
import com.codahale.metrics.health.{HealthCheck, HealthCheckRegistry}
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar

@RunWith(classOf[JUnitRunner])
class HealthCheckSpec extends FunSpec with ShouldMatchers {
  import MockitoSugar._

  describe("healthCheck factory method") {
    it ("registers the created checker") {
      val check = underTest.createBooleanHealthCheck { true }
      verify(testRegistry).register("test", check)
    }

    it("support boolean checker returning true") {
      val check = underTest.createBooleanHealthCheck { true }
      check.execute() should be (Result.healthy())
    }

    it("support boolean checker returning false") {
      val check = underTest.createBooleanHealthCheck { false }
      check.execute() should be (Result.unhealthy("FAIL"))
    }

    it("support boolean checker returning true implicitly") {
      val check = underTest.createImplicitBooleanHealthCheck { Success }
      check.execute() should be (Result.healthy())
    }

    it("support boolean checker returning false implicitly") {
      val check = underTest.createImplicitBooleanHealthCheck { Failure }
      check.execute() should be (Result.unhealthy("FAIL"))
    }

    it("support boolean checker returning Right") {
      val check = underTest.createEitherHealthCheck { Right(()) }
      check.execute() should be (Result.healthy())
    }

    it("support boolean checker returning Left") {
      val check = underTest.createEitherHealthCheck { Left(()) }
      check.execute() should be (Result.unhealthy("FAIL"))
    }

    it("support boolean checker returning Right[String]") {
      val check = underTest.createEitherHealthCheck { Right("I am alright") }
      check.execute() should be (Result.healthy("I am alright"))
    }

    it("support boolean checker returning Left[String]") {
      val check = underTest.createEitherHealthCheck { Left("Oops, I am not fine") }
      check.execute() should be (Result.unhealthy("Oops, I am not fine"))
    }

    it("support boolean checker returning Left[Throwable]") {
      val exception: IllegalArgumentException = new IllegalArgumentException()
      val check = underTest.createEitherHealthCheck { Left(exception) }
      check.execute() should be (Result.unhealthy(exception))
    }

    it("support boolean checker returning Result unchanged") {
      val result = Result.healthy()
      val check = underTest.createResultHealthCheck { result }
      check.execute() should be theSameInstanceAs (result)
    }

    it("support boolean checker throwing an exception") {
      val exception: IllegalArgumentException = new IllegalArgumentException()
      val check = underTest.createThrowingHealthCheck(exception)
      check.execute() should be (Result.unhealthy(exception))
    }
  }

  private val testRegistry = mock[HealthCheckRegistry]

  private class UnderTest extends CheckedBuilder {
    val registry = testRegistry

    // Unfortunately we need a helper method for each supported type. If we wanted a single helper method,
    // we would need to repeat the magnet pattern right here in a test class :(

    def createBooleanHealthCheck(checker: Boolean): HealthCheck =
      healthCheck("test", "FAIL") { checker }

    def createImplicitBooleanHealthCheck(checker: Outcome): HealthCheck =
      healthCheck("test", "FAIL") { checker }

    def createEitherHealthCheck(checker: Either[_, _]): HealthCheck =
      healthCheck("test", "FAIL") { checker }

    def createResultHealthCheck(checker: Result): HealthCheck =
      healthCheck("test", "FAIL") { checker }

    def createThrowingHealthCheck(checkerFailure: Throwable): HealthCheck =
      healthCheck("test", "FAIL") {
        def alwaysFails(): Boolean = throw checkerFailure
        alwaysFails()
      }
  }

  private val underTest = new UnderTest()

}

/** Used to test implicit conversion to boolean. */
private sealed trait Outcome
private case object Success extends Outcome
private case object Failure extends Outcome

/** Implicitly convertible to [[scala.Boolean]]. */
private object Outcome {
  implicit def outcome2Boolean(outcome: Outcome): Boolean = outcome match {
    case Success => true
    case Failure => false
  }
}
