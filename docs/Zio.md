## Zio

This page shows how one can use metrics-scala in a project using Zio.

### Setup

Create the following trait which makes an implicit conversion available:

```scala
import java.util.concurrent.TimeUnit
import nl.grons.metrics4.scala.Timer
import zio._
import zio.clock.Clock

trait ZioInstrumented extends DefaultInstrumented {

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

## Timers

Then in a service you time a task as follows:

```scala
class Example extends ZioInstrumented {

  // Database.fetchRows() is of type ZIO[Database, E, Seq[Row]]

  def loadStuff(): ZIO[Database with Clock, E, Seq[Row]] =
    Database.fetchRows().time(metrics.timer("loading"))
}
```

Clock is available in Zio's standard ZEnv.


Previous: [Miscellaneous](Miscellaneous.md) Up: [Manual](Manual.md) 
