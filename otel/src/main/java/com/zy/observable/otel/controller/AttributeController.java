package com.zy.observable.otel.controller;

import io.opentelemetry.api.trace.Span;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liurui
 * @date 2021/12/30 15:53
 */
@RestController
public class AttributeController extends BaseController {

    @GetMapping("/attr")
    public String attr(){
        Span span = Span.current();
        span.setAttribute("attribute.a2", "some value");
        span.setAttribute("func","attr");
        span.setAttribute("app","otel");
        return buildTraceUrl(span.getSpanContext().getTraceId());
    }
}
