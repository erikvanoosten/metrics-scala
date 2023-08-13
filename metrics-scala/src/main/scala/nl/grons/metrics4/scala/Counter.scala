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

import com.codahale.metrics.{Counter => DropwizardCounter}

/**
 * A Scala facade class for [[DropwizardCounter]].
 */
class Counter(metric: DropwizardCounter) {

  /**
   * Wraps partial function pf, incrementing this counter for every execution (defined or not).
   */
  def count[A,B](pf: PartialFunction[A,B]): PartialFunction[A,B] =
     new PartialFunction[A,B] {
       def apply(a: A): B = {
          metric.inc(1)
          pf.apply(a)
       }

       def isDefinedAt(a: A): Boolean = pf.isDefinedAt(a)
     }

  /**
   * Increase this counter at the start of evaluating `f`, decrease when finished, and finally
   * return the result of `f`.
   */
  def countConcurrency[A](f: => A): A = {
    metric.inc(1)
    try {
      f
    } finally {
      metric.dec(1)
    }
  }

  /**
   * Increments this counter by `delta`.
   */
  def +=(delta: Long): Unit = {
    metric.inc(delta)
  }

  /**
   * Decrements this counter by `delta`.
   */
  def -=(delta: Long): Unit = {
    metric.dec(delta)
  }

  /**
   * Increments this counter by `delta` (defaults to `1`).
   */
  def inc(delta: Long = 1): Unit = {
    metric.inc(delta)
  }

  /**
   * Decrements this counter by `delta` (defaults to `1`).
   */
  def dec(delta: Long = 1): Unit = {
    metric.dec(delta)
  }

  /**
   * The current count.
   */
  def count: Long = metric.getCount

}
