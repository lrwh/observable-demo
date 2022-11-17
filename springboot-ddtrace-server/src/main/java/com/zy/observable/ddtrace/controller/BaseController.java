package com.zy.observable.ddtrace.controller;

import com.zy.observable.ddtrace.util.ConstantsUtils;
import datadog.trace.api.DDTags;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

/**
 * @author liurui
 * @date 2022/7/18 15:22
 */
@Component
public abstract class BaseController {

    @GetMapping("/counter")
    @ResponseBody
    public Integer counter() {
        int get = ConstantsUtils.COUNTER.addAndGet(1);
        return get;
    }

    public void buildErrorTrace(Exception ex) {
        final Span span = GlobalTracer.get().activeSpan();
        if (span != null) {
            span.setTag(Tags.ERROR, true);
            span.log(Collections.singletonMap(Fields.ERROR_OBJECT, ex));
            span.setTag(DDTags.ERROR_MSG, ex.getMessage());
            span.setTag(DDTags.ERROR_TYPE, ex.getClass().getName());

            final StringWriter errorString = new StringWriter();
            ex.printStackTrace(new PrintWriter(errorString));
            span.setTag(DDTags.ERROR_STACK, errorString.toString());
        }

    }
}
