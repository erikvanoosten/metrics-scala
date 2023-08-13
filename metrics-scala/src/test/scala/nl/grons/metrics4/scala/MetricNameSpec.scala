/*
 * Copyright (c) 2013-2023 Erik van Oosten
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

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._

class MetricNameSpec extends AnyFunSpec {

  describe("MetricName object") {
    it("concatenates names with a period as separator") {
      MetricName(classOf[MetricName], "part1", "part2").name should equal ("nl.grons.metrics4.scala.MetricName.part1.part2")
    }

    it("skips nulls") {
      MetricName(classOf[MetricName], "part1", null, "part3").name should equal ("nl.grons.metrics4.scala.MetricName.part1.part3")
    }

    it("skips empty strings") {
      MetricName(classOf[MetricName], "part1", "", "part3").name should equal ("nl.grons.metrics4.scala.MetricName.part1.part3")
    }

    it("supports closures") {
      val foo: String => MetricName = s => MetricName(this.getClass)
      foo("").name should equal ("nl.grons.metrics4.scala.MetricNameSpec")
    }

    it("supports objects") {
      MetricNameSpec.ref.name should equal ("nl.grons.metrics4.scala.MetricNameSpec")
    }

    it("supports nested objects") {
      MetricNameSpec.nestedRef.name should equal ("nl.grons.metrics4.scala.MetricNameSpec.Nested")
    }

    it("supports packages") {
      nl.grons.metrics4.scala.ref.name should equal ("nl.grons.metrics4.scala")
    }

    it("supports nested classes") {
      MetricNameSpec.aBaseClass.ref.name should equal ("nl.grons.metrics4.scala.MetricNameSpec.ABaseClass")
    }

    it("supports anonymous classes") {
      MetricNameSpec.anAnonymousClass.ref.name should equal ("nl.grons.metrics4.scala.MetricNameSpec.anon")
    }
  }

  describe("MetricName") {
    it("appends names with a period as separator") {
      MetricName(classOf[MetricName]).append("part1", "part2").name should equal ("nl.grons.metrics4.scala.MetricName.part1.part2")
    }

    it("skips nulls") {
      MetricName(classOf[MetricName]).append("part1", null, "part3").name should equal ("nl.grons.metrics4.scala.MetricName.part1.part3")
    }

    it("skips empty strings") {
      MetricName("part0").append("part1", "", "part3").name should equal ("part0.part1.part3")
      MetricName("").append("part1", "", "part3").name should equal ("part1.part3")
    }
  }
}

object MetricNameSpec {
  object Nested {
    val ref: MetricName = MetricName(this.getClass)
  }
  private val ref: MetricName = MetricName(this.getClass)
  private val nestedRef: MetricName = Nested.ref

  class ABaseClass {
    val ref: MetricName = MetricName(this.getClass)
  }

  private val aBaseClass = new ABaseClass
  private val anAnonymousClass = new ABaseClass {}
}
