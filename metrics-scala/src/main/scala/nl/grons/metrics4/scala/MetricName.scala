/*
 * Copyright (c) 2013-2018 Erik van Oosten
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

import java.util.regex.Pattern

object MetricName {

  // Example weird class name: TestContext$$anonfun$2$$anonfun$apply$TestObject$2$
  // Anonymous subclass example: ActorMetricsSpec$$anonfun$2$$anonfun$apply$mcV$sp$4$$anonfun$8$$anon$1
  private val classNameFilters = {
    // Note: extracted here to compile the pattern only once.
    val dollarDigitsPattern = Pattern.compile("""\$\d*""")
    Seq(
      StringUtils.replace(_: String, "$$anonfun", "."),
      StringUtils.replace(_: String, "$$anon", ".anon"),
      StringUtils.replace(_: String, "$mcV$sp", "."),
      StringUtils.replace(_: String, "$apply", "."),
      dollarDigitsPattern.matcher(_: String).replaceAll("."),
      StringUtils.replace(_: String, ".package.", "."),
      StringUtils.collapseDots(_: String)
    ).reduce(_ andThen _)
  }

  /**
   * Create a metrics name from a [[Class]].
   *
   * Unlike [[com.codahale.metrics.MetricRegistry.name()]] this version supports Scala classes
   * such as objects and closures.
   *
   * @param metricOwner the class that 'owns' the metric
   * @param names the name parts to append, `null`s are filtered out
   * @return a metric (base)name
   */
  def apply(metricOwner: Class[_], names: String*): MetricName =
    new MetricName(removeScalaParts(metricOwner.getName)).append(names: _*)

  /**
   * Directly create a metrics name from a [[String]].
   *
   * @param name the (base)name for the metric
   * @param names the name parts to append, `null`s are filtered out
   * @return a metric (base)name
   */
  def apply(name: String, names: String*): MetricName = new MetricName(name).append(names: _*)

  private def removeScalaParts(s: String): String = classNameFilters(s)

}

/**
 * The (base)name of a metric.
 *
 * Constructed via the companion object, e.g. `MetricName(getClass, "requests")`.
 */
class MetricName private (val name: String) {

  /**
   * Extend a metric name.
   *
   * @param names the name parts to append, `null`s are filtered out
   * @return the extended metric name
   */
  def append(names: String*): MetricName = {
    if (names.isEmpty) {
      return this
    }

    val sb = new StringBuilder(name)
    names.view
      .filter(_ != null)
      .filter(_.nonEmpty)
      .foreach { newNamePart =>
        sb.append('.')
        sb.append(newNamePart)
      }
    new MetricName(sb.toString())
  }
}
