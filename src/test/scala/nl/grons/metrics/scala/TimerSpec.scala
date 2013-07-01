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

import org.mockito.Mockito._
import org.scalatest.OneInstancePerTest
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.util.concurrent.TimeUnit

@RunWith(classOf[JUnitRunner])
class TimerSpec extends FunSpec with MockitoSugar with ShouldMatchers with OneInstancePerTest {
  describe("A timer") {
    val metric = mock[com.codahale.metrics.Timer]
    val timer = new Timer(metric)
    val context = mock[com.codahale.metrics.Timer.Context]
    when(metric.time()).thenReturn(context)

    it("times the passed closure") {
      timer.time { 1 }

      verify(metric).time()
      verify(context).stop()
    }

    it("updates the underlying metric") {
      timer.update(1L,TimeUnit.MILLISECONDS)

      verify(metric).update(1L,TimeUnit.MILLISECONDS)
    }

    it("should increment time execution of partial function") {
      val pf:PartialFunction[String,String] = { case "test" => "test" }
      val wrapped = timer.time(pf)
      wrapped("test") should equal ("test")
      verify(metric).time()
      verify(context).stop()
    }
  }
}