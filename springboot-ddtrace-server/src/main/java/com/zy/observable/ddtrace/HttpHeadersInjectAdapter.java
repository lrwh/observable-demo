package com.zy.observable.ddtrace;

import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapInject;
import org.springframework.http.HttpHeaders;

import java.util.Iterator;
import java.util.Map;

public class HttpHeadersInjectAdapter implements TextMapInject {
    HttpHeaders httpHeaders;

    public HttpHeadersInjectAdapter(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }
//
//    @Override
//    public Iterator<Map.Entry<String, String>> iterator() {
//        throw new UnsupportedOperationException("This class should be used only with tracer#inject()");
//    }

    @Override
    public void put(String key, String value) {
        httpHeaders.add(key, value);
    }
}

