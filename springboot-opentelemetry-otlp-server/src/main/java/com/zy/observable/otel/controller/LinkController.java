package com.zy.observable.otel.controller;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liurui
 * @date 2021/12/30 14:45
 */
@RestController
public class LinkController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(LinkController.class);

    @GetMapping("/link")
    public String link() {
        String spanName = "childWithLink";
        Span child = tracer.spanBuilder(spanName)
                .addLink(parentSpan(1))
                .addLink(parentSpan(2))
                .addLink(parentSpan(3))
                .startSpan();
        logger.info("create span:{}",spanName);
        try {
            return buildTraceUrl(child.getSpanContext().getTraceId());
        } finally {
            child.end();
        }
    }

    private SpanContext parentSpan(int i) {
        String spanName = "parentSpan"+i;
        logger.info("create span:{}",spanName);
        Span span = tracer.spanBuilder(spanName).startSpan();
        try {
            span.addEvent("this is "+spanName);
            return span.getSpanContext();
        } finally {
            span.end();
        }
    }

}
