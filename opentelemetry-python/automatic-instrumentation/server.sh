#!/usr/bin/env bash

opentelemetry-instrument \
--exporter_jaeger_endpoint http://tempo:14268/api/traces?format=jaeger.thrift \
--service_name python-thrift-server-automatic \
--traces_exporter jaeger_thrift,console \
python $OTEL_WORKDIR/automatic-instrumentation/server.py