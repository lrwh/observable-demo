//package com.zy.observable.otel.filter;
//
//import io.opentelemetry.api.OpenTelemetry;
//import io.opentelemetry.api.trace.Span;
//import io.opentelemetry.api.trace.SpanKind;
//import io.opentelemetry.api.trace.StatusCode;
//import io.opentelemetry.api.trace.Tracer;
//import io.opentelemetry.context.Context;
//import io.opentelemetry.context.Scope;
//import io.opentelemetry.context.propagation.TextMapGetter;
//import io.opentelemetry.context.propagation.TextMapPropagator;
//import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.*;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.List;
//
///**
// * @author liurui
// * @date 2021/12/30
// */
//@Component
//public class TraceFilter implements Filter {
//    @Value("${spring.application.name:'none'}")
//    private String appName;
//    @Autowired
//    OpenTelemetry openTelemetry;
//    @Autowired
//    private Tracer tracer;
//
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
//        Span span = getServerSpan(tracer, httpServletRequest);
//        try (Scope scope = span.makeCurrent()) {
//            filterChain.doFilter(servletRequest, servletResponse);
//        } catch (Exception ex) {
//            span.setStatus(StatusCode.ERROR, "HTTP Code: " + ((HttpServletResponse)servletResponse).getStatus());
//            span.recordException(ex);
//            throw ex;
//        } finally {
//            span.end();
//        }
//    }
//
//    private Span getServerSpan(Tracer tracer, HttpServletRequest httpServletRequest) {
//        TextMapPropagator textMapPropagator = getOpenTelemetry().getPropagators().getTextMapPropagator();
//        Context context = textMapPropagator.extract(Context.current(), httpServletRequest, new TextMapGetter<HttpServletRequest>() {
//            @Override
//            public Iterable<String> keys(HttpServletRequest request) {
//                List<String> headers = new ArrayList();
//                for (Enumeration names = request.getHeaderNames(); names.hasMoreElements();) {
//                    String name = (String)names.nextElement();
//                    headers.add(name);
//                }
//                return headers;
//            }
//            @Override
//            public String get(HttpServletRequest request, String s) {
//                return request.getHeader(s);
//            }
//        });
//        return tracer.spanBuilder(httpServletRequest.getRequestURI()).setParent(context).setSpanKind(SpanKind.SERVER)
//                .setAttribute("filter","tracingFilter")
//                .setAttribute(SemanticAttributes.HTTP_METHOD, httpServletRequest.getMethod()).startSpan();
//    }
//
//    public String getAppName() {
//        return appName;
//    }
//
//    public void setAppName(String appName) {
//        this.appName = appName;
//    }
//
//    public OpenTelemetry getOpenTelemetry() {
//        return openTelemetry;
//    }
//
//    public void setOpenTelemetry(OpenTelemetry openTelemetry) {
//        this.openTelemetry = openTelemetry;
//    }
//}