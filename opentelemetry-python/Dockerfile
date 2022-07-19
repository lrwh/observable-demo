FROM tonycody/python3-otel:1.0

ENV OTEL_WORKDIR=/opentelemetry
WORKDIR $OTEL_WORKDIR

COPY automatic-instrumentation/ automatic-instrumentation/
COPY manual-instrumentation/ manual-instrumentation/
COPY bootstrap.sh .

RUN ln -s $OTEL_WORKDIR/bootstrap.sh /usr/local/bin/entrypoint

ENTRYPOINT ["entrypoint"]
CMD ["zsh"]