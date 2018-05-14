/*
 * Copyright (c) 2014-2018 Erik van Oosten
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

/**
  * By default, metric (and health check) names are prefixed with the name of the current class. The prefix is called
  * the metric base name.
  *
  * Here is an example on how to override the metric base name:
  * {{{
  * class Example(db: Database) extends DefaultInstrumented {
  *   override lazy val metricBaseName = MetricName.build("Overridden.Base.Name")
  *   private[this] val loading = metrics.timer("loading")
  *
  *   def loadStuff(): Seq[Row] = loading.time {
  *     db.fetchRows()
  *   }
  * }
  * }}}
  *
  * Building on the previous example, you can add tags that will be copied to all metrics created in this class:
  * {{{
  *   import nl.grons.metrics5.scala.Implicits._
  *   override lazy val metricBaseName = MetricName
  *     .build("Overridden.Base.Name")
  *     .tagged(Map("tag" -> "tagValue"))
  * }}}
  *
  * If you just want to set some tags do this:
  * {{{
  *   import nl.grons.metrics5.scala.Implicits._
  *   override lazy val metricBaseName = MetricBaseName(getClass).tagged(Map("tag" -> "tagValue"))
  * }}}
  *
  */
trait BaseBuilder {
  /** The base name for metrics and health checks created from this builder. */
  lazy val metricBaseName: MetricName = MetricBaseName(getClass)
}
