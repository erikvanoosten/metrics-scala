## Actor support

    Actor is available since metrics-scala v3.0.1 but only in the Akka versions.

Three [Stackable Traits](http://www.artima.com/scalazine/articles/stackable_trait_pattern.html) are available to quickly
instrument actors.  In all cases, the actual actor implementation must be mixed in *prior* to these stackable traits.

These all follow a common pattern (assuming the `Instrumented` trait exists as described earlier in the documentation):

```scala
trait MyActor extends Actor {
  def receive = {
    case "foo" => sender() ! "bar"
    case "bar" =>
        sender() ! "baz"
        context.stop(self)
  }
}

class MyActorInstrumented extends MyActor with Instrumented
                          with ReceiveCounterActor with ReceiveTimerActor with ReceiveExceptionMeterActor
```

### Receive counter (ReceiveCounterActor)

This trait provides a counter of messages received by the actor.  This count will only increment if the message matches
the `receive` partial function.

### Receive timer (ReceiveTimerActor)

This trait provides a timer of the execution of the `receive` partial function.  Because it's a timer, you get a call meter
and call counter for free.  This is part of the dropwizard metrics library.

### Receive exception meter (ReceiveExceptionMeterActor)

This trait provides an exception meter around the `receive` partial function.  This does not differentiate exceptions by
`Supervisor` configuration.  Any exception thrown by `receive` will be counted equally, ignoring whether the `Supervisor` chooses to
`restart`, `escalate`, or other.

Previous: [Instrumenting Futures](/docs/Futures.md) Up: [Manual](/docs/Manual.md) Next: [Hdrhistogram](/docs/Hdrhistogram.md)
