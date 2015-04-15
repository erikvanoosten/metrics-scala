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

import com.codahale.metrics.health.HealthCheck.Result
import com.codahale.metrics.health.{HealthCheck, HealthCheckRegistry}
import scala.util.{Failure, Success, Try}

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
   * The code block must have a result of type `Boolean`, `Try`, `Either` or
   * [[com.codahale.metrics.health.HealthCheck.Result]].
   *
   *  - A check result of `true` indicates healthy, `false` indicates unhealthy.
   *  - A check result of type [[Success]] indicates healthy, [[Failure]] indicates
   *    unhealthy. The embedded value (after applying `.toString`) or throwable is used as (un)healthy message.
   *  - A check result of type [[Right]] indicates healthy, [[Left]]`[Any]` or [[Left]]`[Throwable]` indicates
   *    unhealthy. The embedded value (after applying `.toString`) or throwable is used as (un)healthy message.
   *  - If the check result is of type [[com.codahale.metrics.health.HealthCheck.Result]], the result is passed
   *    unchanged.
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
  def healthCheck[T](name: String, unhealthyMessage: String = "Health check failed")(checker: => T)(implicit toMagnet: ByName[T] => HealthCheckMagnet): HealthCheck = {
    val magnet = toMagnet(ByName(checker))
    val check = magnet(unhealthyMessage)
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
  import scala.concurrent.{Await, Future}
  import scala.concurrent.duration._
  import scala.language.implicitConversions

  /**
   * Magnet for checkers returning a [[scala.Unit]].
   *
   * If the `checker` throws an exception the check is considered failed, otherwise a success.
   */
  implicit def fromUnitCheck(checker: ByName[Unit]): HealthCheckMagnet =
    fromTryChecker(ByName(Try(checker())))

  /**
   * Magnet for checkers returning a [[scala.concurrent.Future]].
   *
   * The check will block waiting for the [[scala.concurrent.Future]] to complete. It is given a 3-second default
   * timeout after which the [[scala.concurrent.Future]] will be considered a failure and the health check will
   * consequently fail.
   */
  implicit def fromFutureCheck[T](check: ByName[Future[T]])(implicit timeout: Duration = 3.seconds): HealthCheckMagnet =
    //TODO: Remove asInstanceOf when Scala 2.10 support is no longer required.
    fromTryChecker(ByName(Await.ready(check(), timeout).asInstanceOf[Future[T]].value.get))

  /**
   * Magnet for checkers returning a [[scala.Boolean]].
   */
  implicit def fromBooleanCheck[T](checker: ByName[T])(implicit convert: T => Boolean): HealthCheckMagnet = {
    val mapped = checker map convert
    new HealthCheckMagnet {
      def apply(unhealthyMessage: String) = new HealthCheck() {
        protected def check: Result =
          if (mapped()) Result.healthy()
          else Result.unhealthy(unhealthyMessage)
      }
    }
  }

  /**
   * Magnet for checkers returning an [[scala.util.Try]].
   */
  implicit def fromTryChecker[T](checker: ByName[Try[T]]): HealthCheckMagnet = new HealthCheckMagnet {
    def apply(unhealthyMessage: String) = new HealthCheck() {
      protected def check: Result = checker() match {
        case Success(m) => Result.healthy(m.toString)
        case Failure(t) => Result.unhealthy(t)
      }
    }
  }

  /**
   * Magnet for checkers returning an [[scala.util.Either]].
   */
  implicit def fromEitherChecker[T, U](checker: ByName[Either[T, U]]): HealthCheckMagnet = new HealthCheckMagnet {
    def apply(unhealthyMessage: String) = new HealthCheck() {
      protected def check: Result = checker() match {
        case Right(m) => Result.healthy(m.toString)
        case Left(t: Throwable) => Result.unhealthy(t)
        case Left(m) => Result.unhealthy(m.toString)
      }
    }
  }

  /**
   * Magnet for checkers returning a [[com.codahale.metrics.health.HealthCheck.Result]].
   */
  implicit def fromMetricsResultCheck(checker: ByName[Result]): HealthCheckMagnet = new HealthCheckMagnet {
    def apply(unhealthyMessage: String) = new HealthCheck() {
      protected def check: Result = checker()
    }
  }
}
