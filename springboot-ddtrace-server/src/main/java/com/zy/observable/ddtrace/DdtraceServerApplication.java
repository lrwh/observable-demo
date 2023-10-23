package com.zy.observable.ddtrace;

import datadog.trace.api.DDTraceId;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.util.GlobalTracer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class DdtraceServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DdtraceServerApplication.class, args);
    }

    private static void b3TraceByMultiple(){
        String traceId = DDTraceId.from("6917954032704516265").toHexString();
        Tracer tracer = GlobalTracer.get();
        String parentId = DDTraceId.from("4025816492133344807").toHexString();
        for (int i = 0; i < 3; i++) {
            Map<String, String> data = new HashMap<>();
//            data.put("x-datadog-trace-id", traceId);
//            data.put("x-datadog-parent-id",parentId);
            data.put("X-B3-TraceId", traceId);
            data.put("X-B3-SpanId", parentId);
//            data.put("b3.traceid", traceId);
//            data.put("b3.spanid", parentId);
//            SpanContext extract = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapAdapter(data));
//            System.out.println(extract.toTraceId());

            SpanContext extractedContext = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(data));
            Span serverSpan = tracer.buildSpan("opt"+i)
                    .withTag("service","someService"+i)
                    .asChildOf(extractedContext)
                    .start();
            serverSpan.setTag("code","200");
            tracer.activateSpan(serverSpan).close();
            serverSpan.finish();
            parentId = DDTraceId.from(serverSpan.context().toSpanId()).toHexString();
            System.out.println( traceId+"\t"+serverSpan.context().toTraceId()+"\t"+parentId);
        }

    }
    private static void b3TraceBySingle(){
        String traceId = DDTraceId.from("6917954032704516265").toHexString();
        Tracer tracer = GlobalTracer.get();
        String parentId = DDTraceId.from("4025816492133344807").toHexString();
        for (int i = 0; i < 3; i++) {
            String b3 = traceId+ "-"+parentId+"-1";
            Map<String, String> data = new HashMap<>();
            data.put("b3",b3);
            SpanContext extractedContext = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(data));
            Span serverSpan = tracer.buildSpan("opt"+i)
                    .withTag("service","someService"+i)
                    .asChildOf(extractedContext)
                    .start();
            serverSpan.setTag("code","200");
            tracer.activateSpan(serverSpan).close();
            serverSpan.finish();
            parentId = DDTraceId.from(serverSpan.context().toSpanId()).toHexString();
            System.out.println( traceId+"\t"+serverSpan.context().toTraceId()+"\t"+parentId);
            System.out.println("b3="+b3);
        }

    }


    @Bean
    public RestTemplate httpTemplate(){
        return new RestTemplate();
    }
}
