package com.zy.observable.otel.controller;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author liurui
 * @date 2021/12/30 15:03
 */
@RestController
public class EventController extends BaseController{
    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    @GetMapping("/event")
    public String event(){
        Span span = Span.current();
        span.addEvent("no body event");
        span.addEvent("timeEvent",System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        span.addEvent("attrEvent.start", atttributes("startAttr"));
        span.addEvent("attrEvent.end", atttributes("endAttr"));
        return buildTraceUrl(span.getSpanContext().getTraceId());
    }
    private Attributes atttributes(String id) {
        return Attributes.of(AttributeKey.stringKey("app.id"), id);
    }
}
