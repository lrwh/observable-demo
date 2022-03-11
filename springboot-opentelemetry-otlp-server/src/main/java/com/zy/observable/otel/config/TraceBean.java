package com.zy.observable.otel.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author liurui
 * @date 2021/12/30
 */
@Configuration
public class TraceBean {
    @Autowired
    private TraceConfig config;
    @Value("${spring.application.name}")
    private String appName;

    @Bean
    @ConditionalOnBean(TraceConfig.class)
    public OpenTelemetry openTelemetry() {
        SpanProcessor spanProcessor = getJaegerGrpcSpanProcessor();
        Resource serviceNameResource = Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, appName));

        // Set to process the spans by the Zipkin Exporter
        SdkTracerProvider tracerProvider =
                SdkTracerProvider.builder()
                        .addSpanProcessor(spanProcessor)
                        .setResource(Resource.getDefault().merge(serviceNameResource))
                        .build();
        OpenTelemetrySdk openTelemetry =
                OpenTelemetrySdk.builder().setTracerProvider(tracerProvider)
                        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                        .buildAndRegisterGlobal();

        // add a shutdown hook to shut down the SDK
        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));

        // return the configured instance so it can be used for instrumentation.
        return openTelemetry;
    }

    private SpanProcessor getJaegerGrpcSpanProcessor(){
        String httpUrl = String.format("http://%s:%s", config.getHost(), config.getPort());
        System.out.println(httpUrl);
        OtlpGrpcSpanExporter grpcSpanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(httpUrl)   //配置.setEndpoint参数时，必须添加https或者http
                .setTimeout(2, TimeUnit.SECONDS)
                //.addHeader("header1", "1") // 添加header
                .build();
        return BatchSpanProcessor.builder(grpcSpanExporter)
                .setScheduleDelay(100, TimeUnit.MILLISECONDS)
                .build();
    }

    @Bean
    public Tracer tracer(){
        return openTelemetry().getTracer(appName);
    }
}
