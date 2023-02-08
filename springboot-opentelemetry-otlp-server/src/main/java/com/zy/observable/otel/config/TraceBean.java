package com.zy.observable.otel.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleGauge;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
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
        return AutoConfiguredOpenTelemetrySdk.builder()
                .setResultAsGlobal(false)
                .build()
                .getOpenTelemetrySdk();
    }


    private SpanProcessor getJaegerGrpcSpanProcessor() {
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
    public Tracer tracer() {
        return openTelemetry().getTracer(appName);
    }

    @Bean
    public Meter meter() {
        return openTelemetry().getMeter(appName);
    }

    /**
     * @Description 使用 otel sdk 构造 metric 信息
     * @Param []
     * @return void
     **/
    @Bean
    public void customMetrics() {
        meter().gaugeBuilder("connections")
                .setDescription("当前Socket.io连接数")
                .setUnit("1")
                .buildWithCallback(
                        result -> {
                            System.out.println("metrics");
                            for (int i = 1; i < 4; i++) {
                                result.record(
                                        i,
                                        Attributes.of(
                                                AttributeKey.stringKey("id"),
                                                "a" + i));
                            }
                        });
    }
}
