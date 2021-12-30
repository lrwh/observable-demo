package com.zy.observable.otelclent.controller;

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

    @Value("${trace.exporter.host}")
    public String exporterHost;
    @Value("${trace.exporter.uiPort}")
    public String exporterUiPort;
    @Value("${server.port}")
    public String serverPort;

    public String buildTraceUrl(String traceId){
        return "<a href='http://"+exporterHost+":"+serverPort+"/getTrace?traceId="+traceId+"'>"+traceId+"</a>";
    }
}
