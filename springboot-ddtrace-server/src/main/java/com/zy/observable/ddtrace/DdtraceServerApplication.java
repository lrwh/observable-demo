package com.zy.observable.ddtrace;

import datadog.opentracing.DDTracer;
import datadog.trace.api.Config;
import datadog.trace.api.DDId;
import datadog.trace.api.IdGenerationStrategy;
import datadog.trace.api.time.SystemTimeSource;
import datadog.trace.api.time.TimeSource;
import datadog.trace.bootstrap.instrumentation.api.AgentPropagation;
import datadog.trace.bootstrap.instrumentation.api.AgentSpan;
import datadog.trace.bootstrap.instrumentation.api.AgentTracer;
import datadog.trace.common.writer.LoggingWriter;
import datadog.trace.core.*;
import datadog.trace.core.propagation.ExtractedContext;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.propagation.TextMapInject;
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
//        // agent 方式
//        Tracer tracer = GlobalTracer.get();
////
////
////
////        Config config = Config.get();
//        System.out.println(datadog.trace.api.GlobalTracer.get().getClass().getName());
////        DDTracer tracer = DDTracer.builder().build();
////        GlobalTracer.register(tracer);
////////        // register the same tracer with the Datadog API
////        datadog.trace.api.GlobalTracer.registerIfAbsent(tracer);
//
//        CoreTracer trace1 = CoreTracer.builder().build();
//        CoreTracer.CoreSpanBuilder coreSpanBuilder = trace1.buildSpan("core-main");
//        AgentSpan agentSpan = coreSpanBuilder.start();
//        agentSpan.setTag("u1","test");
//
////        Tracer.SpanBuilder spanBuilder = tracer.buildSpan("pp-main");
////        Span span = spanBuilder.start();
//
////        Tracer.SpanBuilder spanBuilder = tracer.buildSpan("b-main");
////        Span span = spanBuilder.start();
////        span.setTag("u","test");
////        span.finish();
////
//        AgentPropagation.Setter<TextMapInject> setter = new AgentPropagation.Setter<TextMapInject>(){
//            @Override
//            public void set(TextMapInject textMapInject, String s, String s1) {
//                textMapInject.put(s,s1);
//            }
//        };
////
////        setter.set(new TextMapInject(){
////            @Override
////            public void put(String s, String s1) {
////                System.out.println("do here");
////            }
////        },"x-datadog-trace-id","7933352457362366894");
//
//        TextMapAdapter adapter = new TextMapAdapter(new HashMap<>());
//        setter.set(adapter,"x-datadog-trace-id","7933352457362366894");
//        setter.set(adapter,"tid","7933352457362366894");
//        trace1.inject(agentSpan.context(),adapter,setter);
//
//
////        AgentTracer.propagate().inject(agentSpan.context(),adapter,setter);
////        AgentPropagation.Setter setter = new AgentPropagation.Setter<TextMapInject>(){
////            @Override
////            public void set(TextMapInject textMapInject, String s, String s1) {
////                textMapInject.put("x-datadog-trace-id","7933352457362366894");
////            }
////        };
////        trace1.inject(agentSpan, TextMapInject.class,setter);
//        agentSpan.finish();
//        System.out.println(agentSpan.getTraceId());
//
////        tracer.inject(span.context(),new RequestBuilderInjectAdapter(),setter);
////        CoreTracer coreTracer = CoreTracer.builder().build();
//
////        MockTracer mockTracer = new MockTracer();
//        DDId traceId = DDId.from("1928095778727968409");
////        PendingTrace pendingTrace = new PendingTrace(trace1,traceId,PendingTraceBuffer.delaying(SystemTimeSource.INSTANCE), SystemTimeSource.INSTANCE.getCurrentTimeNanos(),true);
//        PendingTrace pendingTrace = null;
//        DDSpanContext context = new DDSpanContext(traceId, IdGenerationStrategy.RANDOM.generate(),DDId.from("128010972467255442"),"test","main","","mainResource",0,0,null,null,false,"entry",0,pendingTrace,null,false);
////        AgentSpan agentSpan = coreTracer.startSpan("p-main",context,true);
////        agentSpan.finish();
////
//////        ExtractedContext context = new ExtractedContext(DDId.from("1928095778727968409"), IdGenerationStrategy.RANDOM.generate(),);
////        DDSpan span = new DDSpan(timestampMicro, context, emitCheckpoints);
////        context.getTrace().registerSpan(span);

//        System.out.println(DDId.from("4093897978933045285").toHexStringOrOriginal());
//        treeSpan();
//        b3TraceBySingle();
    }

    private static void b3TraceByMultiple(){
        String traceId = DDId.from("6917954032704516265").toHexStringOrOriginal();
        Tracer tracer = GlobalTracer.get();
        String parentId = DDId.from("4025816492133344807").toHexStringOrOriginal();
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
            parentId = DDId.from(serverSpan.context().toSpanId()).toHexStringOrOriginal();
            System.out.println( traceId+"\t"+serverSpan.context().toTraceId()+"\t"+parentId);
        }

    }
    private static void b3TraceBySingle(){
        String traceId = DDId.from("6917954032704516265").toHexStringOrOriginal();
        Tracer tracer = GlobalTracer.get();
        String parentId = DDId.from("4025816492133344807").toHexStringOrOriginal();
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
            parentId = DDId.from(serverSpan.context().toSpanId()).toHexStringOrOriginal();
            System.out.println( traceId+"\t"+serverSpan.context().toTraceId()+"\t"+parentId);
            System.out.println("b3="+b3);
        }

    }


    @Bean
    public RestTemplate httpTemplate(){
        return new RestTemplate();
    }
}
