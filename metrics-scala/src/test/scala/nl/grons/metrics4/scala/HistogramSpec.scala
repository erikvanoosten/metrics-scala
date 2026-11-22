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

import org.scalamock.scalatest.MockFactory
import org.scalatest.OneInstancePerTest
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._

class HistogramSpec extends AnyFunSpec with OneInstancePerTest with MockFactory {
  describe("A histogram") {
    val metric = stub[TestHistogram]
    val histogram = new Histogram(metric)

    it("updates the underlying histogram with an int") {
      histogram += 12

      (metric.update(_: Int)).verify(12).once()
    }

    it("updates the underlying histogram with a long") {
      histogram += 12L

      (metric.update(_: Long)).verify(12L).once()
    }

    it("retrieves a snapshot for statistics") {
      val snapshot = stub[TestSnapshot]
      (() => snapshot.getMax).when().returns(1L)
      (() => metric.getSnapshot).when().returns(snapshot)

      histogram.max should equal (1L)
    }
  }
}
