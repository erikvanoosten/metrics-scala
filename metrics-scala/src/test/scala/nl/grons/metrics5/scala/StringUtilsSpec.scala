/*
 * Copyright (c) 2018 Erik van Oosten
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

import org.scalatest.Matchers._
import org.scalatest.FunSpec

class StringUtilsSpec extends FunSpec {

  describe("collapseDots") {
    it("strips leading dot") {
      StringUtils.collapseDots(".foo.bar") should equal ("foo.bar")
    }

    it("strips trailing dot") {
      StringUtils.collapseDots("foo.bar.") should equal ("foo.bar")
    }

    it("collapses dots at the beginning of the String") {
      StringUtils.collapseDots("....foo.bar") should equal ("foo.bar")
    }

    it("collapses dots at the end of the String") {
      StringUtils.collapseDots("foo.bar....") should equal ("foo.bar")
    }

    it("collapses dots in the middle of the String") {
      StringUtils.collapseDots("foo....bar...baz") should equal ("foo.bar.baz")
    }

    it("doesn't modify an already valid String") {
      StringUtils.collapseDots("foo.bar.baz") should equal ("foo.bar.baz")
    }
  }

  describe("replace") {
    it("doesn't replace anything in empty Strings") {
      StringUtils.replace("", "foo", "bar") should equal ("")
    }

    it("replaces repeated occurrences") {
      StringUtils.replace("queued", "ue", "") should equal ("qd")
    }

    it("doesn't replace non-matching String") {
      StringUtils.replace("queued", "zz", "") should equal ("queued")
    }

    it("can replace with a longer String") {
      StringUtils.replace("abXYab", "ab", "foobar") should equal ("foobarXYfoobar")
    }

    it("will not allow replacing an empty string") {
      an[IllegalArgumentException] shouldBe thrownBy {
        StringUtils.replace("abXYab", "", "a")
      }
    }
  }

}
