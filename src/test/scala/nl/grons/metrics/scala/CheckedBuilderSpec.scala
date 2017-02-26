/*
 * Copyright (c) 2013-2017 Erik van Oosten
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

import java.text.SimpleDateFormat

import com.codahale.metrics.health.HealthCheck.Result
import com.codahale.metrics.health.{HealthCheck, HealthCheckRegistry}
import org.mockito.Mockito.{verify, when}
import org.scalactic.Equality
import org.scalatest.FunSpec
import org.scalatest.Matchers._
import org.scalatest.mockito.MockitoSugar._

import scala.concurrent.{Future, TimeoutException}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.implicitConversions
import scala.util.Try

class HealthCheckSpec extends FunSpec {
  implicit private val resultWithApproximateTimestampEquality = HealthCheckResultWithApproximateTimestampEquality

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
      check.execute() should equal(Result.healthy())
      check.execute() should equal(Result.unhealthy("FAIL"))
      check.execute() should equal(Result.healthy())
      check.execute() should equal(Result.unhealthy("FAIL"))
    }

    it("supports Boolean checker returning true") {
      val check = newCheckOwner.createBooleanHealthCheck { true }
      check.execute() should equal(Result.healthy())
    }

    it("supports Boolean checker returning false") {
      val check = newCheckOwner.createBooleanHealthCheck { false }
      check.execute() should equal(Result.unhealthy("FAIL"))
    }

    it("supports Boolean checker returning true implicitly") {
      val check = newCheckOwner.createImplicitBooleanHealthCheck { Success }
      check.execute() should equal(Result.healthy())
    }

    it("supports Boolean checker returning false implicitly") {
      val check = newCheckOwner.createImplicitBooleanHealthCheck { Failure }
      check.execute() should equal(Result.unhealthy("FAIL"))
    }

    it("supports Try checker returning Success[Long]") {
      val check = newCheckOwner.createTryHealthCheck { Try(123L) }
      check.execute() should equal(Result.healthy("123"))
    }

    it("supports Try checker returning Success(Unit)") {
      val check = newCheckOwner.createTryHealthCheck { Try(()) }
      check.execute() should equal(Result.healthy())
    }

    it("supports Try checker returning Success(null)") {
      val check = newCheckOwner.createTryHealthCheck { Try[String](null) }
      check.execute() should equal(Result.healthy("null"))
    }

    it("supports Try checker returning Failure") {
      val exception: IllegalArgumentException = new IllegalArgumentException()
      val check = newCheckOwner.createTryHealthCheck { Try(throw exception) }
      check.execute() should equal(Result.unhealthy(exception))
    }

    it("supports Either checker returning Right[Long]") {
      val check = newCheckOwner.createEitherHealthCheck { Right(123L) }
      check.execute() should equal(Result.healthy("123"))
    }

    it("supports Either checker returning Left[Boolean]") {
      val check = newCheckOwner.createEitherHealthCheck { Left(true) }
      check.execute() should equal(Result.unhealthy("true"))
    }

    it("supports Either checker returning Right[String]") {
      val check = newCheckOwner.createEitherHealthCheck { Right("I am alright") }
      check.execute() should equal(Result.healthy("I am alright"))
    }

    it("supports Either checker returning Left[String]") {
      val check = newCheckOwner.createEitherHealthCheck { Left("Oops, I am not fine") }
      check.execute() should equal(Result.unhealthy("Oops, I am not fine"))
    }

    it("supports Either checker returning Left[Throwable]") {
      val exception: IllegalArgumentException = new IllegalArgumentException()
      val check = newCheckOwner.createEitherHealthCheck { Left(exception) }
      check.execute() should equal(Result.unhealthy(exception))
    }

    it("supports Result checker returning Result unchanged") {
      val result = Result.healthy()
      val check = newCheckOwner.createResultHealthCheck { result }
      check.execute() should be theSameInstanceAs result
    }

    it("supports checker throwing an exception") {
      val exception: IllegalArgumentException = new IllegalArgumentException()
      val check = newCheckOwner.createThrowingHealthCheck(exception)
      check.execute() should equal(Result.unhealthy(exception))
    }

    it("supports override of metric base name") {
      val checkOwner = new CheckOwner() {
        override lazy val metricBaseName: MetricName = MetricName("OverriddenMetricBaseName")
      }
      val check = checkOwner.createBooleanHealthCheck { true }
      verify(checkOwner.registry).register("OverriddenMetricBaseName.test", check)
    }

    it("supports Unit checker with side-effects (healthy)") {
      var counter = 0
      val sideEffect: () => Unit = () => {
        counter += 1
      }

      val check = newCheckOwner.createUnitHealthCheckWithSideEffect(sideEffect)
      check.execute() should equal(Result.healthy())
      counter should be (1)
      check.execute() should equal(Result.healthy())
      counter should be (2)
    }

    it("supports Unit checker with side-effects (unhealthy)") {
      val checkerFailure = new IllegalArgumentException()
      var counter = 0
      val sideEffect: () => Unit = () => {
        counter += 1
        throw  checkerFailure
      }

      val check = newCheckOwner.createUnitHealthCheckWithSideEffect(sideEffect)
      check.execute() should equal(Result.unhealthy(checkerFailure))
      counter should be (1)
      check.execute() should equal(Result.unhealthy(checkerFailure))
      counter should be (2)
    }

    it("supports Future checker returning a Success(Long)") {
      val check = newCheckOwner.createFutureHealthCheck(200.milliseconds)(Future {
        Thread.sleep(50)
        123L
      })
      check.execute() should equal(Result.healthy("123"))
    }

    it("supports Future checker returning a Failure(exception)") {
      val exception: IllegalArgumentException = new IllegalArgumentException()
      val check = newCheckOwner.createFutureHealthCheck(200.milliseconds)(Future[Long] {
        Thread.sleep(50)
        throw exception
      })
      check.execute() should equal(Result.unhealthy(exception))
    }

    it("supports Future checker not returning in time") {
      val check = newCheckOwner.createFutureHealthCheck(10.milliseconds)(Future {
        Thread.sleep(1000)
        123L
      })
      val checkResult = check.execute()
      checkResult.isHealthy should be (false)
      checkResult.getError shouldBe a[TimeoutException]
    }
  }

  private val newCheckOwner = new CheckOwner()

}

/**
  * [[HealthCheck.Result]] equality for testing purposes:
  * * the timestamp may be off a little bit
  * * the error must be the same instance
  * * details are not checked
  */
private object HealthCheckResultWithApproximateTimestampEquality extends Equality[Result] {
  private def timestampInMillis(result: Result): Long = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(result.getTimestamp).getTime

  def areEqual(a: Result, b: Any): Boolean = b match {
    case r: Result =>
      a.isHealthy == r.isHealthy &&
        (a.getError eq r.getError) &&
        a.getMessage === r.getMessage &&
        timestampInMillis(a) === timestampInMillis(r) +- 200L
    case _ => false
  }
}

private trait SimpleChecker {
  def check(): Boolean
}

private class CheckOwner() extends CheckedBuilder {
  val registry: HealthCheckRegistry = mock[HealthCheckRegistry]

  // Unfortunately we need a helper method for each supported type. If we wanted a single helper method,
  // we would need to repeat the magnet pattern right here in a test class :(

  def createBooleanHealthCheck(checker: => Boolean): HealthCheck =
    healthCheck("test", "FAIL")(checker)

  def createImplicitBooleanHealthCheck(checker: => Outcome): HealthCheck =
    healthCheck("test", "FAIL")(checker)

  def createTryHealthCheck(checker: => Try[_]): HealthCheck =
    healthCheck("test", "FAIL")(checker)

  def createEitherHealthCheck(checker: => Either[_, _]): HealthCheck =
    healthCheck("test", "FAIL")(checker)

  def createResultHealthCheck(checker: => Result): HealthCheck =
    healthCheck("test", "FAIL")(checker)

  def createThrowingHealthCheck(checkerFailure: => Throwable): HealthCheck = {
    def alwaysFails(): Boolean = throw checkerFailure
    healthCheck("test", "FAIL")(alwaysFails())
  }

  def createCheckerHealthCheck(checker: => SimpleChecker): HealthCheck =
    healthCheck("test", "FAIL")(checker.check())

  def createUnitHealthCheckWithSideEffect(sideEffect: () => Unit): HealthCheck = {
    // Tests an inline block because of
    // https://github.com/erikvanoosten/metrics-scala/issues/42,
    // https://github.com/erikvanoosten/metrics-scala/pull/59 and
    // https://issues.scala-lang.org/browse/SI-3237
    healthCheck("test", "FAIL") {
      sideEffect()
      // Force result type of unit:
      ()
    }
  }

  def createFutureHealthCheck(timeout: FiniteDuration)(checker: => Future[_]): HealthCheck = {
    implicit val checkTimeout = timeout
    healthCheck("test", "FAIL")(checker)
  }
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
