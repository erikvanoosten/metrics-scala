/*
 * Copyright (c) 2020-2022 Erik van Oosten
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

import com.codahale.metrics.{SettableGauge => DropwizardSettableGauge}

/**
 * A gauge to which you can push new values.
 *
 * Can only be constructed via [[MetricBuilder.pushGauge]].
 */
class PushGauge[A](metric: DropwizardSettableGauge[A])  {

  /**
   * Push a new value.
   *
   * @param newValue the new value.
   *                 In case `A` is a reference type, pushing a `null` will make reporters
   *                 ignore this metric (verified for the standard reporters: `GraphiteReporter` and
   *                 `CollectdReporter`).
   */
  def push(newValue: A): Unit = metric.setValue(newValue)

  /** Alias for [[push]]. */
  def value_=(newValue: A): Unit = push(newValue)

  /**
   * The current value.
   */
  def value: A = metric.getValue

}
