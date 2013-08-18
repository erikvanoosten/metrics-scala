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

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheckRegistry
import com.codahale.metrics.health.HealthCheck.Result

/**
 * The mixin trait for creating a class which creates health checks.
 *
 * Use it as follows:
 * {{{
 * object Application {
 *   // The application wide health check registry.
 *   val healthCheckRegistry = new com.codahale.metrics.health.HealthCheckRegistry()
 * }
 * trait Checked extends CheckBuilder {
 *   val healthCheckRegistry = Application.healthCheckRegistry
 * }
 *
 * class Example(db: Database) extends Checked {
 *   private[this] val databaseCheck = checks.booleanCheck("database",{ db.isConnected })
 *
 * }
 * }}}
 */
trait CheckBuilder {
  /**
   * The HealthCheckRegistry where created metrics are registered.
   */
  val registry: HealthCheckRegistry

  /**
   * Registers a new health check for a boolean type, possibly converted implicitly.
   *
   * @param name  the name of the health check
   * @param result the body of the health check
   * @param failureMessage an optional failure message
   */
  def booleanCheck[A <% Boolean](name: String, result: => A, failureMessage: String = "Health check failed"): HealthCheck = {
    val check = new BooleanHealthCheck(result, failureMessage)
    registry.register(name, check)
    check
  }

  /**
   * Registers a new health check for an Either type.  Left is considered unhealthy, Right is considered healthy.
   * If Left is of type string it will be used to override the failure message.  If Left extends Throwable,
   * it overrides the failure message.  If Right is of type string it will override and set a healthy message.
   * In all other cases, the type is ignored.
   *
   * @param name  the name of the health check
   * @param result the body of the health check
   * @param failureMessage an optional failure message
   */
  def eitherCheck[A,B](name: String, result: => Either[A,B], failureMessage: String = "Health check failed"): HealthCheck = {
    val check = new EitherHealthCheck(result,failureMessage)
    registry.register(name, check)
    check
  }

}

protected class BooleanHealthCheck[A <% Boolean](result : => A, failureMessage: String) extends HealthCheck {
  protected def check: Result = result match {
    case true => Result.healthy()
    case _ => Result.unhealthy(failureMessage)
  }
}

protected class EitherHealthCheck[A,B](result: => Either[A,B], failureMessage: String) extends HealthCheck {
  protected def check: Result = result match {
    case Right(s:String) => Result.healthy(s)
    case Right(_) => Result.healthy()
    case Left(s: String) => Result.unhealthy(s)
    case Left(t: Throwable) => Result.unhealthy(t)
    case Left(_) => Result.unhealthy(failureMessage)
  }
}
