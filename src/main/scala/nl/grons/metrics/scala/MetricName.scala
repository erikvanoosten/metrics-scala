/*
 * Copyright (c) 2013-2013 Erik van Oosten
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

class MetricName(val name: String) {
  def append(names: String*): MetricName =
    new MetricName((name.split('.') ++ names.filter(_ != null)).filter(_.nonEmpty).mkString("."))
}

object MetricName {
  def apply(name: String, names: String*): MetricName = new MetricName(name).append(names: _*)

  /**
   * Create a metrics name.
   * Unlike [[com.codahale.metrics.MetricRegistry.name()]] this version supports Scala classes
   * such as objects and closures.
   *
   * @param owner the owning class
   * @return owner's classname as a metric name
   */
  def apply(klass: Class[_]): MetricName = new MetricName(removeScalaParts(klass.getName))

  // Example weird class name: TestContext$$anonfun$2$$anonfun$apply$TestObject$2$
  private def removeScalaParts(s: String) =
    s.replaceAllLiterally("$$anonfun", ".")
     .replaceAllLiterally("$apply", ".")
     .replaceAll("""\$\d*""", ".")
     .replaceAllLiterally(".package.", ".")
}