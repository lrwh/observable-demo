package com.zy.observable.ddtrace.service;

import datadog.trace.api.Trace;
import org.springframework.stereotype.Component;

/**
 * @author liurui
 * @date 2022/3/23 11:22
 */
@Component
public class TestService {

    public String getDemo(){
        return "demo";
    }

    @Trace(resourceName = "apiTrace",operationName = "apiTrace")
    public String apiTrace(){
        return "apiTrace";
    }
}
