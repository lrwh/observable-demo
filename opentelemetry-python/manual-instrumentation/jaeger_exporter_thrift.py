import time

from opentelemetry import trace
from opentelemetry.exporter.jaeger import thrift
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import (BatchSpanProcessor,ConsoleSpanExporter)
from opentelemetry.sdk.resources import SERVICE_NAME, Resource

trace.set_tracer_provider(TracerProvider(
    resource=Resource.create({SERVICE_NAME: "python-thrift-manual"})
    ))
tracer = trace.get_tracer(__name__)

jaeger_exporter = thrift.JaegerExporter(
    collector_endpoint="http://tempo:14268/api/traces?format=jaeger.thrift",
)

trace.get_tracer_provider().add_span_processor(
    BatchSpanProcessor(ConsoleSpanExporter())
)

trace.get_tracer_provider().add_span_processor(
    BatchSpanProcessor(jaeger_exporter)
)

# create some spans for testing
with tracer.start_as_current_span("foo") as foo:
    time.sleep(0.1)
    foo.set_attribute("my_atribbute", True)
    foo.add_event("event in foo", {"name": "foo1"})
    with tracer.start_as_current_span(
        "bar", links=[trace.Link(foo.get_span_context())]
    ) as bar:
        time.sleep(0.2)
        bar.set_attribute("speed", 100.0)

        with tracer.start_as_current_span("baz") as baz:
            time.sleep(0.3)
            baz.set_attribute("name", "mauricio")

        time.sleep(0.2)

    time.sleep(0.1)