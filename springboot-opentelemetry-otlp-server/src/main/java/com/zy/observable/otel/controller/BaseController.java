package com.zy.observable.otel.controller;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

/**
 * @author liurui
 * @date 2021/12/30 14:51
 */
public class BaseController {

    @Value("${api.url}")
    public String apiUrl;
    @Autowired
    public RestTemplate httpTemplate;
    @Autowired
    public Tracer tracer;
    @Autowired
    public OpenTelemetry openTelemetry;

    @Value("${extra.host}")
    public String extraHost;
    @Value("${server.port}")
    public String serverPort;

    public String buildTraceUrl(String traceId){
        return "traceId: "+traceId;
    }
}
