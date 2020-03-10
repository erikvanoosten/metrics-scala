## Zio

This page shows a few examples of how one can use metrics-scala in a project using Zio.

### Timers

Make the following implicit conversion available somewhere in your application:

```scala
import java.util.concurrent.TimeUnit
import nl.grons.metrics4.scala.Timer
import zio._
import zio.clock.Clock

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
```

Then in a service you time a task as follows:

```scala
import nl.grons.metrics4.scala.DefaultInstrumented

class Example extends DefaultInstrumented {

  // Database.fetchRows() is of type ZIO[Database, E, Seq[Row]]

  def loadStuff(): ZIO[Database with Clock, E, Seq[Row]] =
    Database.fetchRows().time(metrics.timer("loading"))
}
```

Clock is available in the standard ZEnv.


Previous: [Miscellaneous](Miscellaneous.md) Up: [Manual](Manual.md) 
