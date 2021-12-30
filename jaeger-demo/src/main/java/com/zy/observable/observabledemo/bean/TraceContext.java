package com.zy.observable.observabledemo.bean;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author liurui
 * @date 2021/12/28 11:33
 */
public class TraceContext {
    public static void setField(String key, String value) {
        if (GlobalTracer.isRegistered() && StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            Tracer tracer = GlobalTracer.get();
            tracer.activeSpan().setBaggageItem(key, value);
        }
    }

    public static String getFiled(String key, String defaultValue) {
        if (GlobalTracer.isRegistered() && StringUtils.isNotBlank(key)) {
            Tracer tracer = GlobalTracer.get();
            return tracer.activeSpan().getBaggageItem(key);
        }
        return defaultValue;
    }
}
