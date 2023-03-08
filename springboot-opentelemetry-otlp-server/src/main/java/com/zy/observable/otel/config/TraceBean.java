package com.zy.observable.otel.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liurui
 * @date 2021/12/30
 */
@Configuration
public class TraceBean {
    @Value("${spring.application.name}")
    private String appName;

    /**
     * @Description 推荐使用这种方式。<br>
     * 这种方式的背后提供的是 noop，也就是默认的 provider，会根据启动参数来决定自行构造 所需的 provider。
     * @return io.opentelemetry.api.OpenTelemetry
     **/
//    @Bean
//    public OpenTelemetry openTelemetry() {
//        SdkMeterProvider meterProvider = SdkMeterProvider
//                                            .builder()
//                                            .registerMetricReader(PrometheusHttpServer.create())
//                                            .build();
//                OpenTelemetrySdk openTelemetry =
//                                    OpenTelemetrySdk.builder()
//            //                        .setTracerProvider(tracerProvider)
//                                    .setMeterProvider(meterProvider)
//            //                        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
////                                            .buildAndRegisterGlobal();
//                                            .build();
//
//        // add a shutdown hook to shut down the SDK
//        Runtime.getRuntime().addShutdownHook(new Thread(meterProvider::close));
//
//        return openTelemetry;
//    }
//    @Bean
//    public OpenTelemetry openTelemetry() {
//        AutoConfiguredOpenTelemetrySdkBuilder builder =
//                    AutoConfiguredOpenTelemetrySdk.builder()
//                            .setResultAsGlobal(false);
//        return builder
//                .addMeterProviderCustomizer(
//                        (meterProviderBuilder, configProperties) ->
//                                            meterProviderBuilder.registerMetricReader(PrometheusHttpServer.create()))
//                .build().getOpenTelemetrySdk();
//    }

//        @Bean
//    public OpenTelemetry openTelemetry() {
//
//            AutoConfiguredOpenTelemetrySdkBuilder builder =
//                    AutoConfiguredOpenTelemetrySdk.builder()
//                            .setResultAsGlobal(false);
//
////            SdkMeterProvider sdkMeterProvider =
////                    builder
////                            .addMeterProviderCustomizer(
////                                    (meterProviderBuilder, configProperties) ->
////                                            meterProviderBuilder.registerMetricReader(PrometheusHttpServer.create()))
////                            .build()
////                            .getOpenTelemetrySdk()
////                            .getSdkMeterProvider();
//
//
//
//
////            return AutoConfiguredOpenTelemetrySdk.builder()
////                    .setResultAsGlobal(false) // 这里填写false，否则会与全局javaagent 冲突
////                    .build()
////                    .getOpenTelemetrySdk();
//        return builder.build().getOpenTelemetrySdk();
//    }

    // 这种方式，主要提供了初始 provider 实现来构建一个 OpenTelemetry 实例。
//    @Bean
//    @ConditionalOnBean(TraceConfig.class)
//    public OpenTelemetry openTelemetry() {
//        SpanProcessor spanProcessor = getJaegerGrpcSpanProcessor();
//        Resource serviceNameResource = Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, appName));
//
//        // Set to process the spans by the Zipkin Exporter
////        SdkTracerProvider tracerProvider =
////                SdkTracerProvider.builder()
////                        .addSpanProcessor(spanProcessor)
////                        .setResource(Resource.getDefault().merge(serviceNameResource))
////                        .build();
//        OpenTelemetrySdk openTelemetry =
//                OpenTelemetrySdk.builder()
////                        .setTracerProvider(tracerProvider)
//                        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
//                        .build();
////                        .buildAndRegisterGlobal();
//
//        // add a shutdown hook to shut down the SDK
////        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));
//
//        // return the configured instance so it can be used for instrumentation.
//        return openTelemetry;
//    }
//    private SpanProcessor getJaegerGrpcSpanProcessor() {
//        String httpUrl = String.format("http://%s:%s", "localhost", "4317");
////        String httpUrl = String.format("http://%s:%s", config.getHost(), config.getPort());
//        System.out.println(httpUrl);
//        OtlpGrpcSpanExporter grpcSpanExporter = OtlpGrpcSpanExporter.builder()
//                .setEndpoint(httpUrl)   //配置.setEndpoint参数时，必须添加https或者http
//                .setTimeout(2, TimeUnit.SECONDS)
//                //.addHeader("header1", "1") // 添加header
//                .build();
//        return BatchSpanProcessor.builder(grpcSpanExporter)
//                .setScheduleDelay(100, TimeUnit.MILLISECONDS)
//                .build();
//    }
    @Bean
    public Tracer tracer() {
        return GlobalOpenTelemetry.getTracer(appName);
    }

    @Bean
    public Meter meter() {
        return GlobalOpenTelemetry.getMeter(appName);
    }

}
