import { setupOTelSDK, createSpanWithTraceParent } from './create-span'

// 初始化 OpenTelemetry SDK
setupOTelSDK()

// 使用traceparent创建span, 可以在请求 request header中获取
const traceParent = '00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01'
const spanName = 'test-span'

console.log('开始创建 span...')
const span = createSpanWithTraceParent(traceParent, spanName)
console.log('span 创建完成!')

// 等待一段时间确保 span 被导出
setTimeout(() => {
  console.log('程序执行完成')
  process.exit(0)
}, 2000)
