/*
 * Copyright (c) 2013-2021 Erik van Oosten
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

import com.codahale.metrics.{Gauge => DropwizardGauge}

object Gauge {
  def apply[A](f: => A) = new Gauge[A](new DropwizardGauge[A] {
    def getValue: A = f
  })
}

/**
 * A Scala facade class for [[DropwizardGauge]].
 */
class Gauge[T](metric: DropwizardGauge[T]) {

  /**
   * The current value.
   */
  def value: T = metric.getValue

}
