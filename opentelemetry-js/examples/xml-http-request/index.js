import { context, trace } from '@opentelemetry/api';
import { ConsoleSpanExporter, SimpleSpanProcessor } from '@opentelemetry/sdk-trace-base';
import { WebTracerProvider } from '@opentelemetry/sdk-trace-web';
import { XMLHttpRequestInstrumentation } from '@opentelemetry/instrumentation-xml-http-request';
import { ZoneContextManager } from '@opentelemetry/context-zone';
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http';
// const { JaegerExporter } = require('@opentelemetry/exporter-jaeger');
const { ZipkinExporter } = require('@opentelemetry/exporter-zipkin');
import { B3Propagator } from '@opentelemetry/propagator-b3';
import { registerInstrumentations } from '@opentelemetry/instrumentation';
import { Resource } from '@opentelemetry/resources';
const { SemanticResourceAttributes } = require('@opentelemetry/semantic-conventions');


const zipkinOptions = {
  url: 'http://192.168.91.11:9411/api/v2/spans', // optional
}

const zipkinExporter = new ZipkinExporter(zipkinOptions);

const otelExporter = new OTLPTraceExporter({
  // optional - url default value is http://localhost:55681/v1/traces
  url: 'http://192.168.91.11:4318/v1/traces',
  headers: {},
});

const providerWithZone = new WebTracerProvider({
      resource: new Resource({
        [SemanticResourceAttributes.SERVICE_NAME]: 'front-app',
      }),
    }
);
// const providerWithZone = new WebTracerProvider({
//   resource: new Resource({
//     "server.name": "front",
//   }),
// });

// Note: For production consider using the "BatchSpanProcessor" to reduce the number of requests
// to your exporter. Using the SimpleSpanProcessor here as it sends the spans immediately to the
// exporter without delay
// providerWithZone.addSpanProcessor(new SimpleSpanProcessor(zipkinExporter));
providerWithZone.addSpanProcessor(new SimpleSpanProcessor(new ConsoleSpanExporter()));
providerWithZone.addSpanProcessor(new SimpleSpanProcessor(otelExporter));
/**
 *  jaegerExporter error 
ERROR in ./node_modules/thriftrw/tstruct.js 26:15-39
Module not found: Error: Can't resolve 'util' in 'D:\code_zy\opentelemetry-js\examples\tracer-web\node_modules\thriftrw'

BREAKING CHANGE: webpack < 5 used to include polyfills for node.js core modules by default.
This is no longer the case. Verify if you need this module and configure a polyfill for it.

If you want to include a polyfill, you need to:
        - add a fallback 'resolve.fallback: { "util": require.resolve("util/") }'
        - install 'util'
If you don't want to include a polyfill, you can use an empty module like this:
        resolve.fallback: { "util": false }
 @ ./node_modules/thriftrw/index.js 27:14-34
 @ ./node_modules/jaeger-client/dist/src/thrift.js 34:16-35
 @ ./node_modules/@opentelemetry/exporter-jaeger/build/src/types.js 25:0-70
 @ ./node_modules/@opentelemetry/exporter-jaeger/build/src/jaeger.js 24:20-38
 @ ./node_modules/@opentelemetry/exporter-jaeger/build/src/index.js 28:13-32
 @ ./examples/xml-http-request/index.js 10:4-45

ERROR in ./node_modules/thriftrw/union.js 23:11-26
Module not found: Error: Can't resolve 'util' in 'D:\code_zy\opentelemetry-js\examples\tracer-web\node_modules\thriftrw'

BREAKING CHANGE: webpack < 5 used to include polyfills for node.js core modules by default.
This is no longer the case. Verify if you need this module and configure a polyfill for it.

If you want to include a polyfill, you need to:
        - add a fallback 'resolve.fallback: { "util": require.resolve("util/") }'
        - install 'util'
If you don't want to include a polyfill, you can use an empty module like this:
        resolve.fallback: { "util": false }
 @ ./node_modules/thriftrw/thrift.js 35:18-48
 @ ./node_modules/thriftrw/index.js 92:0-50
 @ ./node_modules/jaeger-client/dist/src/thrift.js 34:16-35
 @ ./node_modules/@opentelemetry/exporter-jaeger/build/src/types.js 25:0-70
 @ ./node_modules/@opentelemetry/exporter-jaeger/build/src/jaeger.js 24:20-38
 @ ./node_modules/@opentelemetry/exporter-jaeger/build/src/index.js 28:13-32
 @ ./examples/xml-http-request/index.js 10:4-45

58 errors have detailed information that is not shown.
Use 'stats.errorDetails: true' resp. '--stats-error-details' to show it.
webpack 5.72.1 compiled with 58 errors in 9309 ms
 */
// providerWithZone.addSpanProcessor(new SimpleSpanProcessor(jaegerExporter));



providerWithZone.register({
  contextManager: new ZoneContextManager(),
  propagator: new B3Propagator(),
});

registerInstrumentations({
  instrumentations: [
    new XMLHttpRequestInstrumentation({
      ignoreUrls: [/localhost:8090\/sockjs-node/],
      propagateTraceHeaderCorsUrls: [
        'https://httpbin.org/get',
        'http://192.168.91.11:8080/gateway',
        'http://localhost:8080/gateway',
      ],
    }),
  ],
});

const webTracerWithZone = providerWithZone.getTracer('example-tracer-web');

const getData = (url) => new Promise((resolve, reject) => {
  const req = new XMLHttpRequest();
  req.open('GET', url, true);
  req.setRequestHeader('Content-Type', 'application/json');
  req.setRequestHeader('Accept', 'application/json');
  req.onload = () => {
    resolve();
  };
  req.onerror = () => {
    reject();
  };
  req.send();
});

// example of keeping track of context between async operations
const prepareClickEvent = () => {
  // const url1 = 'https://httpbin.org/get';
  // const url1 = 'http://192.168.91.11:8080/gateway';
  const url1 = 'http://localhost:8080/gateway';

  const element = document.getElementById('button1');

  const onClick = () => {
    for (let i = 0, j = 2; i < j; i += 1) {
      const span1 = webTracerWithZone.startSpan(`files-series-info-${i}`);
      context.with(trace.setSpan(context.active(), span1), () => {
        getData(url1).then((_data) => {
          trace.getSpan(context.active()).addEvent('fetching-span1-completed');
          span1.end();
        }, () => {
          trace.getSpan(context.active()).addEvent('fetching-error');
          span1.end();
        });
      });
    }
  };
  element.addEventListener('click', onClick);
};

window.addEventListener('load', prepareClickEvent);
