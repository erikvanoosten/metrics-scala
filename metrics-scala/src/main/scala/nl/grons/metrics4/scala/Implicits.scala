/*
 * Copyright (c) 2013-2026 Erik van Oosten
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
 * This object held implicit conversions of Scala functions to Metric interfaces in earlier
 * versions of metrics-scala. These conversions are no longer needed as Scala 2.12 and later
 * have [[https://www.scala-lang.org/news/2.12.0/#lambda-syntax-for-sam-types SAM]].
 */
@deprecated("No longer needed, remove the import", "4.4.0")
object Implicits
