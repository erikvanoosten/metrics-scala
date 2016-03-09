## Actor support

    Actor support is available since metrics-scala v3.0.1 but only in the Akka versions.

A number of [Stackable Traits](http://www.artima.com/scalazine/articles/stackable_trait_pattern.html) are available to
quickly instrument actors.  In all cases, the actual actor implementation must be mixed in *prior* to these stackable
traits.

Usage follows a common pattern (the `Instrumented` trait is described earlier in the documentation):

```scala
trait MyActor extends Actor with Instrumented {
  def receive = {
    case "foo" => sender() ! "bar"
    case "bar" =>
        sender() ! "baz"
        context.stop(self)
  }
}

class MyActorInstrumented extends MyActor with
                          with ActorInstrumentedLifeCycle
                          with ReceiveCounterActor with ReceiveTimerActor with ReceiveExceptionMeterActor
```

### Life cycle support for gauges: `ActorInstrumentedLifeCycle`

Getting the `IllegalArgumentException "A metric named some-gauge already exists"`?

When an actor is restarted, gauges can not be created again under the same name in the same metric registry.
By mixing in the `ActorInstrumentedLifeCycle` trait, all gauges created in this actor will be automatically
unregistered before this actor restarts.

    `ActorInstrumentedLifeCycle` is available since metrics-scala v3.5.3.

### Receive counter: `ReceiveCounterActor`

This trait provides a counter of messages received by the actor.  This count will only increment if the message matches
the `receive` partial function.

### Receive timer: `ReceiveTimerActor`

This trait provides a timer of the execution of the `receive` partial function.  Because it's a timer, you get a call
meter and call counter for free.  This is part of the dropwizard metrics library.

### Receive exception meter: `ReceiveExceptionMeterActor`

This trait provides an exception meter around the `receive` partial function.  This does not differentiate exceptions
by `Supervisor` configuration.  Any exception thrown by `receive` will be counted equally, ignoring whether the
`Supervisor` chooses to `restart`, `escalate`, or other.

Previous: [Instrumenting Futures](/docs/Futures.md) Up: [Manual](/docs/Manual.md) Next: [Hdrhistogram](/docs/Hdrhistogram.md)
