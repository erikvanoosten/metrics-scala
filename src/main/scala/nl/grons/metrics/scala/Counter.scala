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

import com.codahale.metrics.{Counter => DropwizardCounter}

/**
 * A Scala facade class for [[DropwizardCounter]].
 */
class Counter(metric: DropwizardCounter) {

  /**
   * Wraps partial function pf, incrementing counter once for every execution
   */
   def count[A,B](pf: PartialFunction[A,B]): PartialFunction[A,B] =
     new PartialFunction[A,B] {
       def apply(a: A): B = {
          metric.inc(1)
          pf.apply(a)
       }

       def isDefinedAt(a: A) = pf.isDefinedAt(a)
     }

  /**
   * Increments the counter by delta.
   */
  def +=(delta: Long) {
    metric.inc(delta)
  }

  /**
   * Decrements the counter by delta.
   */
  def -=(delta: Long) {
    metric.dec(delta)
  }

  /**
   * Increments the counter by 1.
   */
  def inc(delta: Long = 1) {
    metric.inc(delta)
  }

  /**
   * Decrements the counter by 1.
   */
  def dec(delta: Long = 1) {
    metric.dec(delta)
  }

  /**
   * The current count.
   */
  def count: Long = metric.getCount

}
