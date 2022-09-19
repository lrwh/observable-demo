package com.zy.observable.ddtrace;

import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapExtract;
import io.opentracing.propagation.TextMapInject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HttpRequestExtractAdapter implements TextMapExtract {
    private final HttpHeaders httpHeaders;

    public HttpRequestExtractAdapter(final HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        Iterator<String> iterator = httpHeaders.keySet().iterator();
        while (iterator.hasNext()){
            String key = iterator.next();
            System.out.println("key - header:\t"+key+":"+httpHeaders.get(key).get(0));;
        }
        return null;
    }

//    @Override
//    public void put(final String key, final String value) {
//        throw new UnsupportedOperationException("This class should be used only with Tracer.extract()!");
//    }
}
