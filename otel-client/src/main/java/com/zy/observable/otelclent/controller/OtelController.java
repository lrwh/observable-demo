package com.zy.observable.otelclent.controller;

import io.opentelemetry.api.trace.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author liurui
 * @date 2021/12/29 17:42
 */
@Controller
public class OtelController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(OtelController.class);


    @GetMapping("/client")
    @ResponseBody
    public String client() {
        Span.current().updateName("客户端");
        logger.info("this is 客户端");
        System.out.println(Span.current().getSpanContext().getTraceId());
        return "this is 客户端";
    }
}
