package nl.grons.metrics.scala

import com.codahale.metrics.JmxReporter
import com.codahale.metrics.Slf4jReporter
import org.slf4j.Marker
import org.slf4j.Logger
import javax.management.MBeanServer
import com.codahale.metrics.MetricFilter
import java.util.concurrent.TimeUnit
import com.codahale.metrics.ConsoleReporter
import com.codahale.metrics.CsvReporter
import java.io.File
import javax.management.MBeanServerFactory
import java.lang.management.ManagementFactory
import com.codahale.metrics.{MetricRegistry => CHMetricRegistry, Gauge => CHGauge} 


object Reporting {
  type Builder = { 
    def filter(f: MetricFilter): Any
    def convertDurationsTo(u: TimeUnit): Any
    def convertRatesTo(u: TimeUnit):Any
  }
}

object MetricRegistry {
  implicit def scalaRegistry2CodahaleRegistry(registry: MetricRegistry) = registry.metricsRegistry
}

trait MetricRegistry {  
  protected def registryName = CHMetricRegistry.name("metrics")
  
  lazy protected val metricsRegistry = new com.codahale.metrics.MetricRegistry()
  lazy private[this] val klass = getClass.getSuperclass
  
  private[this] def metricName(name: String, maybeScope: Option[String] = None) = maybeScope match {
      case Some(scope) => CHMetricRegistry.name(klass,name,scope)
      case None => CHMetricRegistry.name(klass,name)
  }

  /**
   * Registers a new gauge metric.
   *
   * @param name  the name of the gauge
   * @param scope the scope of the gauge
   * @param registry the registry for the gauge
   */
  def gauge[A](name: String, maybeScope: Option[String] = None)(f: => A) =
    Gauge[A](metricsRegistry.register(metricName(name,maybeScope), new CHGauge[A] { def getValue: A = f }))

  /**
   * Creates a new counter metric.
   *
   * @param name  the name of the counter
   * @param scope the scope of the gauge
   * @param registry the registry for the gauge
   */
  def counter(name: String, maybeScope: Option[String] = None) = 
		  new Counter(metricsRegistry.counter(metricName(name, maybeScope)))

  /**
   * Creates a new histogram metrics.
   *
   * @param name   the name of the histogram
   * @param scope  the scope of the histogram
   */
  def histogram(name: String, maybeScope: Option[String] = None) =
    new Histogram(metricsRegistry.histogram(metricName(name, maybeScope)))

  /**
   * Creates a new meter metric.
   *
   * @param name the name of the meter
   * @param eventType the plural name of the type of events the meter is
   *                  measuring (e.g., "requests")
   * @param scope the scope of the meter
   */
  def meter(name: String, maybeScope: Option[String] = None) =
    new Meter(metricsRegistry.meter(metricName(name, maybeScope)))

  /**
   * Creates a new timer metric.
   *
   * @param name the name of the timer
   * @param scope the scope of the timer
   */
  def timer(name: String, maybeScope: Option[String] = None) =
    new Timer(metricsRegistry.timer(metricName(name, maybeScope)))
  
}

trait Instrumented extends MetricRegistry

trait Reporting[T] { self : MetricRegistry =>
  import Reporting._
  
  /**
   * Builds reporter
   */
  protected def build: T
  
  /**
   * Applies filter, duration, rate conversion to reporter
   */
  protected final def apply(builder: Builder) = {
    filter.map { f =>
      builder.filter(f)
    } 

    duration.map { d =>
      builder.convertDurationsTo(d)
    }
    
    rate.map { r =>
      builder.convertRatesTo(r)
    }
    
    ()
  }
  
  /**
   * Filter definition - defaults to None
   */
  protected def filter : Option[MetricFilter] = None

  /**
   * Duration conversion definition - defaults to None
   */
  protected def duration : Option[TimeUnit] = None

  /**
   * Rate conversion definition - defaults to None
   */
  protected def rate : Option[TimeUnit] = None
}

trait JmxReporting extends Reporting[JmxReporter] { self: MetricRegistry =>
  
  build
  
  /**
   * MBean server used for JMX - defaults to ManagementFactory's platform server
   */
  def mbeanServer = ManagementFactory.getPlatformMBeanServer()
  
  /**
   * Domain to be used for JMX beans - defaults to None
   */
  def domain: Option[String] = None
  
  protected def build = {
    val builder = JmxReporter.forRegistry(metricsRegistry).registerWith(mbeanServer)
    apply(builder)
    (domain match {
      case Some(d) => builder.inDomain(d)
      case None => builder
    }).build()
  }
}

trait Slf4JReporting extends Reporting[Slf4jReporter] { self: MetricRegistry =>
  
  build

  /**
   * Logger to be used for SLF4J reporter
   */
  val logger: Logger
  
  /**
   * Optional marker to be used for SLF4J reporter
   */
  def marker: Option[Marker] = None
  
  protected def build = {
    val builder = Slf4jReporter.forRegistry(metricsRegistry).outputTo(logger)
    apply(builder)
    (marker match {
      case Some(m) => builder.markWith(m)
      case None => builder
    }).build()
  }
}

trait ConsoleReporting extends Reporting[ConsoleReporter] { self: MetricRegistry =>
  
  build
  
  /**
   * Where to write console results - defaults to System.out
   */
  def outputTo = System.out
  
  protected def build = {
    val builder = ConsoleReporter.forRegistry(metricsRegistry).outputTo(outputTo)
    apply(builder)
    builder.build()
  }
}

trait CsvReporting extends Reporting[CsvReporter] { self: MetricRegistry =>
  
  build
  
  /**
   * Where to write csv results
   */
  val outputTo : File
  
  protected def build = {
    val builder = CsvReporter.forRegistry(metricsRegistry)
    apply(builder)
    builder.build(outputTo)
  }
}