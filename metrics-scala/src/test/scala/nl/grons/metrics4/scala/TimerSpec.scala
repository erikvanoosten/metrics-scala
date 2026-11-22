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

import java.util.concurrent.TimeUnit
import com.codahale.metrics.TestTimerContext
import org.scalamock.scalatest.MockFactory
import org.scalatest.OneInstancePerTest
import org.scalatest.OptionValues._
import org.scalatest.TryValues._
import org.scalatest.concurrent.Eventually
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._

import scala.concurrent.{ExecutionContext, Future}

object TimerSpec {
  case class Result()

  def myFunc(x: String): List[Result] = {
    List(Result())
  }

}

class TimerSpec extends AnyFunSpec with OneInstancePerTest with MockFactory {

  import TimerSpec._

  describe("A timer") {
    val metric = stub[TestTimer]
    val timer = new Timer(metric)
    val context = stub[TestTimerContext]
    (() => metric.time()).when().returns(context)

    it("times the passed closure") {
      timer.time { 1 }

      (() => metric.time()).verify().once()
      (() => context.stop()).verify().once()
    }

    it("updates the underlying metric") {
      timer.update(1L,TimeUnit.MILLISECONDS)

      (metric.update(_: Long, _: TimeUnit)).verify(1L, TimeUnit.MILLISECONDS).once()
    }

    it("should increment time execution of partial function") {
      val pf: PartialFunction[String,String] = { case "test" => "test" }
      val wrapped = timer.timePF(pf)
      wrapped("test") should equal ("test")
      (() => metric.time()).verify().once()
      (() => context.stop()).verify().once()
      wrapped.isDefinedAt("x") should be (false)
    }

    it("should measure a future") {
      import ExecutionContext.Implicits.global
      val f = timer.timeFuture(Future.successful("test"))

      Eventually.eventually {
        (() => metric.time()).verify().once()
        (() => context.stop()).verify().once()
      }

      f.value.value.success.value should equal ("test")
    }

    it("should measure a future closure which errors") {
      import ExecutionContext.Implicits.global
      val error = new Exception
      val caught = intercept[Exception] {
        timer.timeFuture { throw error }
      }
      caught should equal (error)

      (() => metric.time()).verify().once()
      (() => context.stop()).verify().once()
    }

    it("correctly infers the type") {
      val someString = "someString"
      val timed = timer.time(myFunc(someString))
      timed.isInstanceOf[List[_]] should be (true)
      timed.head.isInstanceOf[Result] should be (true)

      val pf: PartialFunction[String,String] = { case x: String => x }
      val timedPF = timer.timePF( pf )
      timedPF.isInstanceOf[PartialFunction[_,_]] should be (true)
      timedPF("x") should be ("x")
    }
  }
}
