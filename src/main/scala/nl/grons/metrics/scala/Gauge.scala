/*
 * Copyright (c) 2013-2015 Erik van Oosten
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

import com.codahale.metrics.{Gauge => CHGauge}

object Gauge {
  def apply[A](f: => A) = new Gauge[A](new CHGauge[A] {
    def getValue = f
  })
}

/**
 * A Scala façade class for Gauge.
 */
class Gauge[T](private val metric: CHGauge[T]) {

  /**
   * The current value.
   */
  def value: T = metric.getValue

}
