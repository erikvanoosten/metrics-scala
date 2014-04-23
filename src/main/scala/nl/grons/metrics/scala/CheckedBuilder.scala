/*
 * Copyright (c) 2013-2014 Erik van Oosten
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

import com.codahale.metrics.health.HealthCheck.Result
import com.codahale.metrics.health.{HealthCheck, HealthCheckRegistry}

/**
 * The mixin trait for creating a class which creates health checks.
 */
trait CheckedBuilder extends BaseBuilder {
  /**
   * The [[com.codahale.metrics.health.HealthCheckRegistry]] where created metrics are registered.
   */
  val registry: HealthCheckRegistry

  /**
   * Converts a code block to a [[com.codahale.metrics.health.HealthCheck]] and registers it.
   *
   * Use it as follows:
   * {{{
   * object Application {
   *   // The application wide health check registry.
   *   val healthCheckRegistry = new com.codahale.metrics.health.HealthCheckRegistry()
   * }
   * trait Checked extends CheckedBuilder {
   *   val healthCheckRegistry = Application.healthCheckRegistry
   * }
   *
   * class Example(db: Database) extends Checked {
   *   private[this] val databaseCheck = healthCheck("database") { db.isConnected }
   * }
   * }}}
   *
   * The code block must have a result of type `Boolean`, `Either`, or
   * [[com.codahale.metrics.health.HealthCheck.Result]].
   *
   *  - A check result of `true` indicates healthy, `false` indicates unhealthy.
   *  - A check result of type `Right[AnyVal]` indicates healthy, `Left[AnyVal]` or `Left[Throwable]` indicates
   *    unhealthy. The embedded value (after applying `.toString`) or throwable is used as (un)healthy message.
   *  - If the check result is of type `Result`, the result is passed unchanged.
   *  - In case the code block throws an exception, the result is considered 'unhealthy'.
   *
   *
   * It is also possible to override the health check base name. For example:
   * {{{
   * class Example(db: Database) extends Checked {
   *   override lazy val metricBaseName = MetricName("Overridden.Base.Name")
   *   private[this] val databaseCheck = healthCheck("database") { db.isConnected }
   * }
   * }}}
   *
   * @param name the name of the health check
   * @param unhealthyMessage the unhealthy message for checkers that return `false`, defaults to `"Health check failed"`
   * @param checker the code block that does the health check
   */
  def healthCheck(name: String, unhealthyMessage: String = "Health check failed")(checker: HealthCheckMagnet): HealthCheck = {
    val check = checker(unhealthyMessage)
    registry.register(metricBaseName.append(name).name, check)
    check
  }
}

/**
 * Magnet for the checker.
 * See [[http://spray.io/blog/2012-12-13-the-magnet-pattern/]].
 */
sealed trait HealthCheckMagnet {
  def apply(unhealthyMessage: String): HealthCheck
}

object HealthCheckMagnet {
  /**
   * Magnet for checkers returning a [[scala.Boolean]] (possibly implicitly converted).
   */
  implicit def fromBooleanCheck[A <% Boolean](checker: => A) = new HealthCheckMagnet {
    def apply(unhealthyMessage: String) = new HealthCheck() {
      protected def check: Result =
        if (checker) Result.healthy()
        else Result.unhealthy(unhealthyMessage)
    }
  }

  /**
   * Magnet for checkers returning an [[scala.util.Either]].
   */
  implicit def fromEitherChecker(checker: => Either[_, _]) = new HealthCheckMagnet {
    def apply(unhealthyMessage: String) = new HealthCheck() {
      protected def check: Result = checker match {
        case Right(m) => Result.healthy(m.toString)
        case Left(t: Throwable) => Result.unhealthy(t)
        case Left(m) => Result.unhealthy(m.toString)
      }
    }
  }

  /**
   * Magnet for checkers returning a [[com.codahale.metrics.health.HealthCheck.Result]].
   */
  implicit def fromMetricsResultCheck(checker: => Result) = new HealthCheckMagnet {
    def apply(unhealthyMessage: String) = new HealthCheck() {
      protected def check: Result = checker
    }
  }
}
