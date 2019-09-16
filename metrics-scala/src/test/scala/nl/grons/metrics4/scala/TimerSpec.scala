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

import java.util.concurrent.TimeUnit

import org.mockito.IdiomaticMockito._
import matchers.should.Matchers._
import org.scalatest.OneInstancePerTest
import org.scalatest.OptionValues._
import org.scalatest.TryValues._
import org.scalatest.concurrent.Eventually
import org.scalatest.funspec.AnyFunSpec

import scala.concurrent.{ExecutionContext, Future}
import org.scalatest.matchers

object TimerSpec {
  case class Result()

  def myFunc(x: String): List[Result] = {
    List(Result())
  }

}

class TimerSpec extends AnyFunSpec with OneInstancePerTest {

  import TimerSpec._

  describe("A timer") {
    val metric = mock[com.codahale.metrics.Timer]
    val timer = new Timer(metric)
    val context = mock[com.codahale.metrics.Timer.Context]
    metric.time() shouldReturn context

    it("times the passed closure") {
      timer.time { 1 }

      metric.time() was called
      context.stop() was called
    }

    it("updates the underlying metric") {
      timer.update(1L,TimeUnit.MILLISECONDS)

      metric.update(1L,TimeUnit.MILLISECONDS)
    }

    it("should increment time execution of partial function") {
      val pf: PartialFunction[String,String] = { case "test" => "test" }
      val wrapped = timer.timePF(pf)
      wrapped("test") should equal ("test")
      metric.time() was called
      context.stop() was called
      wrapped.isDefinedAt("x") should be (false)
    }

    it("should measure a future") {
      import ExecutionContext.Implicits.global
      val f = timer.timeFuture(Future.successful("test"))

      Eventually.eventually { context.stop() was called }

      f.value.value.success.value should equal ("test")
      metric.time() was called
      context.stop() was called
    }

    it("should measure a future closure which errors") {
      import ExecutionContext.Implicits.global
      val error = new Exception
      val caught = intercept[Exception] {
        timer.timeFuture { throw error }
      }
      caught should equal (error)

      metric.time() was called
      context.stop() was called
    }

    it("correctly infers the type") {
      val someString = "someString"
      val timed = timer.time(myFunc(someString))
      timed.isInstanceOf[List[_]] should be (true)
      timed(0).isInstanceOf[Result] should be (true)

      val pf: PartialFunction[String,String] = { case x: String => x }
      val timedPF = timer.timePF( pf )
      timedPF.isInstanceOf[PartialFunction[_,_]] should be (true)
      timedPF("x") should be ("x")
    }
  }
}