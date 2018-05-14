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

package nl.grons.metrics5.scala

import io.dropwizard.metrics5.MetricName

object MetricBaseName {

  /**
    * Create a metrics name from a [[Class]].
    *
    * Unlike [[io.dropwizard.metrics5.MetricRegistry.name()]] this version supports Scala classes
    * such as objects and closures.
    *
    * Example invocation:
    * {{{
    *   val metricBaseName = MetricBaseName(getClass)
    * }}}
    *
    * @param metricOwner the class that 'owns' the metric
    * @return a metric (base)name
    */
  def apply(metricOwner: Class[_]): MetricName =
    MetricName.build(removeScalaParts(metricOwner.getName))

  // Example weird class name: TestContext$$anonfun$2$$anonfun$apply$TestObject$2$
  // Anonymous subclass example: ActorMetricsSpec$$anonfun$2$$anonfun$apply$mcV$sp$4$$anonfun$8$$anon$1
  private def removeScalaParts(s: String) =
    s.replaceAllLiterally("$$anonfun", ".")
      .replaceAllLiterally("$$anon", ".anon")
      .replaceAllLiterally("$mcV$sp", ".")
      .replaceAllLiterally("$apply", ".")
      .replaceAll("""\$\d*""", ".")
      .replaceAllLiterally(".package.", ".")
      .split('.')
      .filter(_.nonEmpty)
      .mkString(".")

}
