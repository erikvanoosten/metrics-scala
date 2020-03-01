/*
 * Copyright (c) 2013-2020 Erik van Oosten
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

/**
 * A type class that defines a zero for all Scala types.
 * Used by [[MetricBuilder.pushGauge]] and [[MetricBuilder.pushGaugeWithTimeout]].
 */
trait Zero[A] {
  def zero: A
}

trait ZeroImplicits {

  implicit def anyRefZero[A <: AnyRef]: Zero[A] = new Zero[A] {
    override val zero: A = null.asInstanceOf[A]
  }

  implicit object BooleanZero extends Zero[Boolean] {
    val zero: Boolean = false
  }

  implicit object CharZero extends Zero[Char] {
    val zero: Char = '\u0000'
  }

  implicit object ByteZero extends Zero[Byte] {
    val zero: Byte = 0
  }

  implicit object ShortZero extends Zero[Short] {
    val zero: Short = 0
  }

  implicit object IntZero extends Zero[Int] {
    val zero: Int = 0
  }

  implicit object LongZero extends Zero[Long] {
    val zero: Long = 0L
  }

  implicit object FloatZero extends Zero[Float] {
    val zero: Float = 0f
  }

  implicit object DoubleZero extends Zero[Double] {
    val zero: Double = 0d
  }

}
