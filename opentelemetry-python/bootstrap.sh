#!/usr/bin/env bash

echo -e "\033[41;37m ----------- Starting the sample automatic-instrumentation ----------- \033[0m"
echo -e "\033[41;33m Starting Server... \033[0m"
. $OTEL_WORKDIR/automatic-instrumentation/server.sh 2>&1 &
sleep 5s
echo -e "\033[41;33m Starting Client... \033[0m"
. $OTEL_WORKDIR/automatic-instrumentation/client.sh

echo -e "\033[41;37m ----------- Starting the sample manual-instrumentation ----------- \033[0m"
echo -e "\033[41;33m Starting jaeger exporter grpc... \033[0m"
. $OTEL_WORKDIR/manual-instrumentation/jaeger_exporter_grpc.sh
echo -e "\033[41;33m Starting jaeger exporter thrift... \033[0m"
. $OTEL_WORKDIR/manual-instrumentation/jaeger_exporter_thrift.sh

exec "$@"