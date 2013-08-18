package nl.grons.metrics.scala

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.OneInstancePerTest
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.codahale.metrics.health.HealthCheckRegistry
import com.codahale.metrics.health.HealthCheck.Result

sealed trait Outcome
case object Success extends Outcome
case object Failure extends Outcome

object HealthCheckSpec {

  implicit def outcome2Boolean(outcome: Outcome) = outcome match {
    case Success => true
    case Failure => false
  }

}

@RunWith(classOf[JUnitRunner])
class HealthCheckSpec extends FunSpec with MockitoSugar with ShouldMatchers {

  var counter = 0

  def sometimesISucceed = {
    counter += 1
    counter % 2 == 0
  }

  def amIHealthy(health: Boolean) = {
    () => health
  }

  def isThisAHealthyOutcome(health: Boolean):() => Outcome = health match {
    case true => () => Success
    case false => () => Failure
  }

  def eitherHealthyOrNot[A](health: Boolean, content: A): () => Either[A,A] = health match {
    case true => () => Right(content)
    case false => () => Left(content)
  }

  val testRegistry = mock[HealthCheckRegistry]

  class Checked extends CheckBuilder {
    import HealthCheckSpec._

    val registry = testRegistry

    def booleanChecked(name: String, failureMessage: String, healthy: Boolean) = {
      booleanCheck(name, amIHealthy(healthy)(), failureMessage)
    }

    def implicitBooleanCheck(name: String, failureMessage: String, healthy: Boolean) = {
      checks.booleanCheck(name, isThisAHealthyOutcome(healthy)(), failureMessage)
    }

    def sometimesSuccessfulCheck(name: String, failureMessage: String) = {
      checks.booleanCheck(name, sometimesISucceed, failureMessage)
    }

    def eitherChecked[A](name: String, content: A, failureMessage: String, healthy: Boolean) = {
      checks.eitherCheck(name, eitherHealthyOrNot(healthy, content)(), failureMessage)
    }
  }

  val checks = new Checked()

  describe("a boolean health check") {

    it("reports healthy on true") {
      val check = checks.booleanChecked("test", "FAIL", true)
      check.execute() should be (Result.healthy())
      // Should succeed every time
      check.execute() should be (Result.healthy())
    }

    it("reports unhealthy on false") {
      val check = checks.booleanChecked("test", "FAIL", false)
      check.execute() should be (Result.unhealthy("FAIL"))
      // Should fail every time
      check.execute() should be (Result.unhealthy("FAIL"))
    }

    it("supports implicit conversion to boolean") {
      val check = checks.implicitBooleanCheck("test", "FAIL", false)
      check.execute() should be (Result.unhealthy("FAIL"))
      // Should fail every time
      check.execute() should be (Result.unhealthy("FAIL"))
    }

    it("works as expected for alternating values") {
      val check = checks.sometimesSuccessfulCheck("test", "FAIL")
      check.execute() should be (Result.unhealthy("FAIL"))
      check.execute() should be (Result.healthy())
      check.execute() should be (Result.unhealthy("FAIL"))
      check.execute() should be (Result.healthy())
    }
  }

  describe("an either health check") {

    it("reports healthy on right") {
      val check = checks.eitherChecked("test",(),"FAIL",true)
      check.execute() should be (Result.healthy())
      // Should fail every time
      check.execute() should be (Result.healthy())
    }

    it("reports unhealthy on left") {
      val check = checks.eitherChecked("test",(),"FAIL",false)
      check.execute() should be (Result.unhealthy("FAIL"))
      // Should fail every time
      check.execute() should be (Result.unhealthy("FAIL"))
    }

    it("allows override of healthy message")  {
      val check = checks.eitherChecked("test","HUZZAH!","FAIL",true)
      check.execute() should be (Result.healthy("HUZZAH!"))
      // Should fail every time
      check.execute() should be (Result.healthy("HUZZAH!"))
    }

    it("allows override of unhealthy message") {
      val check = checks.eitherChecked("test","BZZZZZZZT!","FAIL",false)
      check.execute() should be (Result.unhealthy("BZZZZZZZT!"))
      // Should fail every time
      check.execute() should be (Result.unhealthy("BZZZZZZZT!"))
    }

  }

}