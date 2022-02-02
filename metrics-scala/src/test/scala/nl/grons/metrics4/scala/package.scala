/*
 * Copyright (c) 2013-2022 Erik van Oosten
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

package nl.grons.metrics4

import org.scalatest.Tag

package object scala {

  object GE_Scala212 extends Tag(">=scala2.12")
  object LT_Scala212 extends Tag("<scala2.12")

  /**
   * Used in [[nl.grons.metrics4.scala.MetricNameSpec]].
   */
  val ref: MetricName = MetricName(this.getClass)

}
