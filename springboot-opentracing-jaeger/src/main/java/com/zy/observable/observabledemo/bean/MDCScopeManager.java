package com.zy.observable.observabledemo.bean;


import com.zy.observable.observabledemo.util.ConstantsUtils;
import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.ThreadLocalScopeManager;
import org.slf4j.MDC;
/**
 * 自定义MDC
 * @author liurui
 * @date 2021/12/28
 */
public class MDCScopeManager extends ThreadLocalScopeManager {

    @Override
    public Scope activate(Span span) {
        Scope activate = super.activate(span);
        mdc(span);
        return activate;
    }


    public void mdc(Span span){
        JaegerSpanContext ctx = (JaegerSpanContext) span.context();
        String traceId = ctx.getTraceId();
        String spanId = Long.toHexString(ctx.getSpanId());
        String sampled = String.valueOf(ctx.isSampled());
        String parentSpanId = Long.toHexString(ctx.getParentId());
        String userId = ctx.getBaggageItem(ConstantsUtils.MDC_USER_ID);
        replace("traceId", traceId);
        replace("spanId", spanId);
        replace("parentSpanId", parentSpanId);
        replace("sampled", sampled);
        replace(ConstantsUtils.MDC_USER_ID,userId);
    }

    private static String lookup(String key) {
        return MDC.get(key);
    }

    private static void replace(String key, String value) {
        if (value == null) {
            MDC.remove(key);
        } else {
            MDC.put(key, value);
        }
    }
}