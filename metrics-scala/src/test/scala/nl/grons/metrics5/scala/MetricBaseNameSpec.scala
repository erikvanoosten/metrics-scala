/*
 * Copyright (c) 2013-2018 Erik van Oosten
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

package nl.grons.metrics5.scala

import io.dropwizard.metrics5.MetricName
import org.scalatest.Matchers._
import org.scalatest.FunSpec

class MetricBaseNameSpec extends FunSpec {

  describe("MetricBaseName.apply") {
    it("concatenates names with a period as separator") {
      MetricBaseName(classOf[MetricBaseNameSpec]).getKey shouldBe "nl.grons.metrics5.scala.MetricBaseNameSpec"
    }

    it("supports closures") {
      val foo: String => MetricName = _ => MetricBaseName(this.getClass)
      foo("").getKey shouldBe "nl.grons.metrics5.scala.MetricBaseNameSpec"
    }

    it("supports objects") {
      MetricBaseNameSpec.ref.getKey shouldBe "nl.grons.metrics5.scala.MetricBaseNameSpec"
    }

    it("supports nested objects") {
      MetricBaseNameSpec.nestedRef.getKey shouldBe "nl.grons.metrics5.scala.MetricBaseNameSpec.Nested"
    }

    it("supports packages") {
      nl.grons.metrics5.scala.ref.getKey shouldBe "nl.grons.metrics5.scala"
    }

    it("supports nested classes") {
      MetricBaseNameSpec.aBaseClass.ref.getKey shouldBe "nl.grons.metrics5.scala.MetricBaseNameSpec.ABaseClass"
    }

    it("supports anonymous classes") {
      MetricBaseNameSpec.anAnonymousClass.ref.getKey shouldBe "nl.grons.metrics5.scala.MetricBaseNameSpec.anon"
    }
  }
}

object MetricBaseNameSpec {
  object Nested {
    val ref: MetricName = MetricBaseName(this.getClass)
  }
  private val ref: MetricName = MetricBaseName(this.getClass)
  private val nestedRef: MetricName = Nested.ref

  class ABaseClass {
    val ref: MetricName = MetricBaseName(this.getClass)
  }

  private val aBaseClass = new ABaseClass
  private val anAnonymousClass = new ABaseClass {}
}
