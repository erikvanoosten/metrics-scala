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

import com.codahale.metrics.health.HealthCheck.Result
import com.codahale.metrics.health.{HealthCheck, HealthCheckRegistry}
import org.junit.runner.RunWith
import org.mockito.Mockito.{when, verify}
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar._

@RunWith(classOf[JUnitRunner])
class HealthCheckSpec extends FunSpec {

  describe("healthCheck factory method") {
    it ("registers the created checker") {
      val checkOwner = newCheckOwner
      val check = checkOwner.createBooleanHealthCheck { true }
      verify(checkOwner.registry).register("nl.grons.metrics.scala.CheckOwner.test", check)
    }

    it("build health checks that call the provided checker") {
      val mockChecker = mock[SimpleChecker]
      when(mockChecker.check()).thenReturn(true, false, true, false)
      val check = newCheckOwner.createCheckerHealthCheck(mockChecker)
      check.execute() should be (Result.healthy())
      check.execute() should be (Result.unhealthy("FAIL"))
      check.execute() should be (Result.healthy())
      check.execute() should be (Result.unhealthy("FAIL"))
    }

    it("supports boolean checker returning true") {
      val check = newCheckOwner.createBooleanHealthCheck { true }
      check.execute() should be (Result.healthy())
    }

    it("supports boolean checker returning false") {
      val check = newCheckOwner.createBooleanHealthCheck { false }
      check.execute() should be (Result.unhealthy("FAIL"))
    }

    it("supports boolean checker returning true implicitly") {
      val check = newCheckOwner.createImplicitBooleanHealthCheck { Success }
      check.execute() should be (Result.healthy())
    }

    it("supports boolean checker returning false implicitly") {
      val check = newCheckOwner.createImplicitBooleanHealthCheck { Failure }
      check.execute() should be (Result.unhealthy("FAIL"))
    }

    it("supports boolean checker returning Right[Long]") {
      val check = newCheckOwner.createEitherHealthCheck { Right(123L) }
      check.execute() should be (Result.healthy("123"))
    }

    it("supports boolean checker returning Left[Boolean]") {
      val check = newCheckOwner.createEitherHealthCheck { Left(true) }
      check.execute() should be (Result.unhealthy("true"))
    }

    it("supports boolean checker returning Right[String]") {
      val check = newCheckOwner.createEitherHealthCheck { Right("I am alright") }
      check.execute() should be (Result.healthy("I am alright"))
    }

    it("supports boolean checker returning Left[String]") {
      val check = newCheckOwner.createEitherHealthCheck { Left("Oops, I am not fine") }
      check.execute() should be (Result.unhealthy("Oops, I am not fine"))
    }

    it("supports boolean checker returning Left[Throwable]") {
      val exception: IllegalArgumentException = new IllegalArgumentException()
      val check = newCheckOwner.createEitherHealthCheck { Left(exception) }
      check.execute() should be (Result.unhealthy(exception))
    }

    it("supports boolean checker returning Result unchanged") {
      val result = Result.healthy()
      val check = newCheckOwner.createResultHealthCheck { result }
      check.execute() should be theSameInstanceAs (result)
    }

    it("supports boolean checker throwing an exception") {
      val exception: IllegalArgumentException = new IllegalArgumentException()
      val check = newCheckOwner.createThrowingHealthCheck(exception)
      check.execute() should be (Result.unhealthy(exception))
    }

    it("supports override of metric base name") {
      val checkOwner = new CheckOwner() {
        override lazy val metricBaseName: MetricName = MetricName("OverriddenMetricBaseName")
      }
      val check = checkOwner.createBooleanHealthCheck { true }
      verify(checkOwner.registry).register("OverriddenMetricBaseName.test", check)
    }
  }

  private val newCheckOwner = new CheckOwner()

}

private trait SimpleChecker {
  def check(): Boolean
}

private class CheckOwner() extends CheckedBuilder {
  val registry: HealthCheckRegistry = mock[HealthCheckRegistry]

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

  def createCheckerHealthCheck(checker: SimpleChecker): HealthCheck =
    healthCheck("test", "FAIL") { checker.check() }
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
