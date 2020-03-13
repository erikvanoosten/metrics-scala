## Zio

This page shows how one can use metrics-scala in a project using Zio.

### Setup

Create the following trait which makes an implicit conversion available:

```scala
import java.util.concurrent.TimeUnit
import nl.grons.metrics4.scala.{Counter, DefaultInstrumented, Timer}
import zio._
import zio.clock.Clock

trait ZioInstrumented extends DefaultInstrumented {

  /**
   * Allows the following syntax:
   *
   * {{{
   *   val counter = metrics.counter("task")
   *   val task: ZIO[R, E, A] = ???
   *
   *   // Count success only:
   *   val task1: ZIO[R, E, A] = task <* counter.increment()
   *   // Count success and failure:
   *   val task2: ZIO[R, E, A] = task ensuring counter.increment()
   * }}}
   */
  implicit class ZioCounter(counter: Counter) {
    def increment(delta: Int = 1): UIO[Unit] = ZIO.effectTotal(counter += delta)
  }

  /**
   * Allows the following syntax:
   *
   * {{{
   *   val timer = metrics.timer("task")
   *   val task: ZIO[R, E, A] = ???
   *
   *   // A new task that also times `task`:
   *   val timedTask: ZIO[R, E, A] = task.time(timer)
   * }}}
   */
  implicit class ZioTime[R, E, A](task: ZIO[R, E, A]) {
    def time(timer: Timer): ZIO[R with Clock, E, A] = {
      task
        .timed
        .map { case (duration, result) =>
          val d = duration.asScala
          if (d.isFinite()) timer.update(d.toNanos, TimeUnit.NANOSECONDS)
          result
        }
    }
  }
}
```

## Counters

You can lazily increase a counter after a task completed:

```scala
class Example extends ZioInstrumented {

  // Database.fetchRows() is of type ZIO[Database, E, Seq[Row]]

  def loadStuff(): ZIO[Database, E, Seq[Row]] =
    Database.fetchRows() <* metrics.counter("query").increment()
}
```

This counts whenever the database query succeeded. Alternatively, to also count failed queries do:

```scala
    Database.fetchRows() ensuring metrics.counter("query").increment()
```

## Timers

This is how to time a Zio task:

```scala
class Example extends ZioInstrumented {

  // Database.fetchRows() is of type ZIO[Database, E, Seq[Row]]

  def loadStuff(): ZIO[Database with Clock, E, Seq[Row]] =
    Database.fetchRows().time(metrics.timer("loading"))
}
```

Clock is available in Zio's standard ZEnv.


Previous: [Miscellaneous](Miscellaneous.md) Up: [Manual](Manual.md) 
