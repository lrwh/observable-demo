import { Resource } from '@opentelemetry/resources'
import { BatchSpanProcessor, WebTracerProvider } from '@opentelemetry/sdk-trace-web'
import { ZoneContextManager } from '@opentelemetry/context-zone'
import { trace, SpanContext, TraceFlags, context } from '@opentelemetry/api'
import { W3CTraceContextPropagator } from '@opentelemetry/core'
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-proto'

const setupOTelSDK = () => {
  const resource = Resource.default().merge(
    new Resource({
      'service.name': 'test',
    })
  )

  const tracerProvider = new WebTracerProvider({
    resource: resource,
  })

  const traceExporter = new OTLPTraceExporter({
    url: 'http://127.0.0.1:9529/otel/v1/traces',
    headers: {},
  })

  const spanProcessor = new BatchSpanProcessor(traceExporter, {
    // 可选配置参数
    maxExportBatchSize: 100, // 每批最多处理的span数量
    scheduledDelayMillis: 1000, // 定期导出的间隔时间(毫秒)
  })

  // propagation.setGlobalPropagator(new W3CTraceContextPropagator());
  // 设置上下文传播器
  const contextManager = new ZoneContextManager()
  tracerProvider.addSpanProcessor(spanProcessor)
  tracerProvider.register({
    contextManager,
    propagator: new W3CTraceContextPropagator(),
  })
  trace.setGlobalTracerProvider(tracerProvider)
  // 添加这段代码来注册仪表
  // registerInstrumentations({
  //   instrumentations: [
  //     new FetchInstrumentation({
  //       // 您可以在这里添加自定义配置
  //       clearTimingResources: true,
  //       propagateTraceHeaderCorsUrls: [/.*/],
  //     }),
  //     new DocumentLoadInstrumentation(),
  //   ],
  //   tracerProvider: tracerProvider,
  // });

  // registerInstrumentations({
  //   instrumentations: [
  //     new FetchInstrumentation({
  //       propagateTraceHeaderCorsUrls: [/.*/],
  //       clearTimingResources: true,
  //     }),
  //     new XMLHttpRequestInstrumentation({
  //       propagateTraceHeaderCorsUrls: [/.*/],
  //     }),
  //   ],
  // });
}

const parseTraceParent = (traceParent: string) => {
  const parts = traceParent.split('-')
  if (parts.length !== 4) throw new Error('Invalid trace_parent format')
  if (parts[0] !== '00') throw new Error('Unsupported trace_parent version')

  const traceId = parts[1]
  const parentSpanId = parts[2]

  if (traceId.length !== 32) throw new Error('traceId must be 32 characters')
  if (parentSpanId.length !== 16) throw new Error('parentSpanId must be 16 characters')
  if (!isHex(traceId)) throw new Error('traceId contains invalid hex characters')
  if (!isHex(parentSpanId)) throw new Error('parentSpanId contains invalid hex characters')

  return [traceId, parentSpanId]
}

const isHex = (s: string) => {
  return /^[0-9a-fA-F]+$/.test(s)
}

const createSpanWithTraceParent = (traceParent: string, spanName: string) => {
  if (!traceParent) return
  const [traceId, parentSpanId] = parseTraceParent(traceParent)
  const tracer = trace.getTracer('Browser')
  // 创建SpanContext
  const spanContext: SpanContext = {
    traceId: traceId,
    spanId: parentSpanId,
    traceFlags: TraceFlags.SAMPLED,
    // traceState: new TraceState(),
    isRemote: true,
  }
  // 包装SpanContext为Span
  const parentSpan = trace.wrapSpanContext(spanContext)
  // 创建父级上下文 - 修正这一行
  const parentContext = trace.setSpan(context.active(), parentSpan)
  // 创建并启动子span
  const childSpan = tracer.startSpan(
    spanName,
    {
      // attributes: {
      //   "parsing time": `${10000/1000}μs`
      // }
    },
    parentContext
  )

  try {
    // console.info(`Child span started with trace_id: ${traceId}`);
    // 业务逻辑...
  } finally {
    childSpan.end()
  }
}

export { setupOTelSDK, createSpanWithTraceParent }
