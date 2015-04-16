package nl.grons.metrics.scala

/**
 * Provides a wrapper for by-name expressions with the intent that they can become eligible for
 * implicit conversions and implicit resolution.
 *
 * @param expression A by-name parameter that will yield a result of type `T` when evaluated.
 * @tparam T Result type of the evaluated `expression`.
 */
final class ByName[+T](expression: => T) extends (() => T) {
  /**
   * Evaluates the given `expression` every time <em>without</em> memoizing the result.
   *
   * @return Result of type `T` when evaluating the provided `expression`.
   */
  def apply(): T = expression

  /**
   * Lazily maps the given expression of type `T` to type `U`.
   */
  def map[U](fn: T => U): ByName[U] =
    ByName[U](fn(expression))
}

object ByName {
  import scala.language.implicitConversions

  /**
   * Implicitly converts a by-name `expression` of type `T` into an instance of [[nl.grons.metrics.scala.ByName]].
   */
  implicit def apply[T](expression: => T): ByName[T] =
    new ByName(expression)
}
