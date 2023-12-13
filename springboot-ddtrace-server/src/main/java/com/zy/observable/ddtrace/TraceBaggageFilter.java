package com.zy.observable.ddtrace;


import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Baggage 可以让 tag 在链路之间进行传递，通过获取当前请求header，将指定前缀的header设置为Baggage
 * @author liurui
 * @date 2022/7/19 14:59
 */
@Component
public class TraceBaggageFilter implements Filter {

    /**
     * 指定前缀的 header 进行链路传递
     */
    private static final String PREFIX = "dd-";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        final Span span = GlobalTracer.get().activeSpan();
        if (span != null) {
            HttpServletRequest request = (HttpServletRequest)servletRequest;
            System.out.println(request.getRequestURI());
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                final String header = headerNames.nextElement();
                String value = request.getHeader(header);
                System.out.println(header+"\t\t\t"+value);
                if (StringUtils.startsWith(header,PREFIX) && StringUtils.isNotBlank(value)){
                    //Baggage 可以在链路之间进行传递，而普通的tag不行
                    span.setBaggageItem(header.replace(PREFIX,""),value);
                }
            }

        }
        filterChain.doFilter(servletRequest,servletResponse);
    }
}
