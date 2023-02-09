package com.zy.observable.otel.controller;

import com.sun.net.httpserver.HttpExchange;
import com.zy.observable.otel.util.ConstantsUtils;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.extension.annotations.WithSpan;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

/**
 * @author liurui
 * @date 2021/12/29 17:42
 */
@Controller
public class OtelController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(OtelController.class);

    @Value("${client:false}")
    private Boolean client;

    /**
     * 自定义span
     *
     * @return
     */

    @GetMapping("/customSpan")
    @ResponseBody
    public String customSpan() {
        // 创建一个子span
        Span span = tracer.spanBuilder("parent").startSpan();
        span.setAttribute("username", "liu");
        try (Scope scope = span.makeCurrent()) {
            sonSpan();
        } catch (Throwable t) {
            span.setStatus(StatusCode.ERROR, "Change it to your error message");
        } finally {
            span.end(); // closing the scope does not end the span, this has to be done manually
        }
        logger.info("traceId:{},spanId:{}", span.getSpanContext().getTraceId(), span.getSpanContext().getSpanId());

        return "success";
    }

    /**
     * 创建一个子span并绑定父span
     *
     * @return
     */
    public String sonSpan() {
        Span sonSpan = tracer.spanBuilder("child")
                .setParent(Context.current().with(Span.current()))
                .startSpan();
        try {
            sonSpan.setAttribute("username", "joy");
            return "";
        } finally {
            sonSpan.end();
        }
    }


    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/gateway")
    @ResponseBody
    public String gateway(String tag) {
        Span span = Span.current().updateName("网关服务");
        String userId = "user-" + System.currentTimeMillis();
        MDC.put(ConstantsUtils.MDC_USER_ID, userId);
        span.addEvent("testEvent");
        Attributes eventAttributes = Attributes.of(
                AttributeKey.stringKey("key"), "value",
                AttributeKey.longKey("result"), 10L);
        logger.info("traceid : {}",span.getSpanContext().getTraceId());
        // Baggage 用法,此处set
        Baggage.current().toBuilder().put("app.username", "gateway").build().makeCurrent();
        logger.info("gateway set baggage[app.username] value: gateway");
        span.addEvent("End Computation", eventAttributes);
        doSomeWorkNewSpan();
        logger.info("this is tag");
        sleep();

        httpTemplate.getForEntity(apiUrl + "/resource", String.class).getBody();
        httpTemplate.getForEntity(apiUrl + "/auth", String.class).getBody();
        if (client) {
            httpTemplate.getForEntity("http://"+extraHost+":8081/client", String.class).getBody();
        }
        return httpTemplate.getForEntity(apiUrl + "/billing?tag=" + tag, String.class).getBody();
    }

    @GetMapping("/resource")
    @ResponseBody
    public String resource() {
        Span.current().updateName("资源服务");
        Span.current().setAttribute("func", "resource");
        // Baggage 用法,此get
        String baggage = Baggage.current().getEntryValue("app.username");
        logger.info("resource get baggage[app.username] value: {}", baggage);
        return "this is resource";
    }

    @GetMapping("/auth")
    @ResponseBody
    public String auth() {
        Span.current().updateName("认证授权");
        logger.info("this is auth");
        sleep();
        return "this is auth";
    }

    @GetMapping("/billing")
    @ResponseBody
    public String billing(String tag) {
        Span.current().updateName("订单");
        logger.info("this is method3,{}", tag);
        sleep();
        if (Optional.ofNullable(tag).get().equalsIgnoreCase("error")) {
            System.out.println(1 / 0);
        }
        SpanContext spanContext = Span.current().getSpanContext();
        return buildTraceUrl(spanContext.getTraceId());
    }

    private void sleep() {

    }


    @WithSpan
    private void doSomeWorkNewSpan() {
        logger.info("Doing some work In New span");
        Span span = Span.current();
        span.setAttribute("attribute.a2", "some value");
        span.setAttribute("func", "doSomeWorkNewSpan");
        span.setAttribute("app", "otel");

        span.addEvent("app.processing2.start", atttributes("321"));
        span.addEvent("app.processing2.end", atttributes("321"));
    }

    private Attributes atttributes(String id) {
        return Attributes.of(AttributeKey.stringKey("app.id"), id);
    }

//    @GetMapping("/context")
//    @ResponseBody
//    public String context() {
//        Context current = Context.current();
//        Span span = Span.fromContext(current);
//        span.addEvent("hello");
//
//        Baggage baggage = Baggage.fromContext(current);
//        baggage.toBuilder().put("username","liu").build();
//
//        System.out.println(span.getSpanContext().getSpanId());
//        return "context";
//    }


    @GetMapping("/context")
    @ResponseBody
    public String context() {
        TextMapSetter<HttpURLConnection> setter =
                new TextMapSetter<HttpURLConnection>() {
                    @Override
                    public void set(HttpURLConnection carrier, String key, String value) {
                        // Insert the context as Header
                        carrier.setRequestProperty(key, value);
                    }
                };
        Span outGoing = tracer.spanBuilder("/sonContext").setSpanKind(SpanKind.CLIENT).startSpan();
        try (Scope scope = outGoing.makeCurrent()) {
            URL url = new URL("http://127.0.0.1:8080/sonContext");
            // Use the Semantic Conventions.
            // (Note that to set these, Span does not *need* to be the current instance in Context or Scope.)
            outGoing.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
//            outGoing.setAttribute(SemanticAttributes.HTTP_URL, url.toString());
            outGoing.setAttribute("attr1", "hh");
            outGoing.setAttribute("username", "joy");
            HttpURLConnection transportLayer = (HttpURLConnection) url.openConnection();
            // Inject the request with the *current*  Context, which contains our current Span.
            setter.set(transportLayer, "contextName", "MyContext");
            openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), transportLayer, setter);
            // Make outgoing call
            InputStream is = transportLayer.getInputStream();
            is.close();
        } catch (Exception e) {

        } finally {
            outGoing.end();
        }
        return "context";
    }

    public void handle(HttpExchange httpExchange) {
        //        TextMapGetter<HttpExchange> getter =

        TextMapGetter<HttpExchange> getter = new TextMapGetter<HttpExchange>() {
            @Override
            public String get(HttpExchange carrier, String key) {
                if (carrier.getRequestHeaders().containsKey(key)) {
                    return carrier.getRequestHeaders().get(key).get(0);
                }
                return null;
            }

            @Override
            public Iterable<String> keys(HttpExchange carrier) {
                return carrier.getRequestHeaders().keySet();
            }
        };
        // Extract the SpanContext and other elements from the request.
        Context extractedContext = openTelemetry.getPropagators().getTextMapPropagator()
                .extract(Context.current(), httpExchange, getter);
        try (Scope scope = extractedContext.makeCurrent()) {
            // Automatically use the extracted SpanContext as parent.
            Span serverSpan = tracer.spanBuilder("GET /resource")
                    .setSpanKind(SpanKind.SERVER)
                    .startSpan();
            try {
                // Add the attributes defined in the Semantic Conventions
                serverSpan.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
                serverSpan.setAttribute(SemanticAttributes.HTTP_SCHEME, "http");
                serverSpan.setAttribute(SemanticAttributes.HTTP_HOST, "localhost:8080");
                serverSpan.setAttribute(SemanticAttributes.HTTP_TARGET, "/resource");
                // Serve the request
            } finally {
                serverSpan.end();
            }
        }
    }

    @RequestMapping("/getClient")
    @ResponseBody
    public String getClient() {
        return result();
    }

    @RequestMapping("/setClient")
    @ResponseBody
    public String setClient(Boolean c) {
        client = c;
        return result();
    }
    private String result(){
        return client ? "【已开启】客户端请求" : "【已关闭】客户端请求";
    }


    /***
     * @Description 通过已知的traceId和spanId,来构造一个新span。 需要特别注意，依据当前测试写法的请求自身会产生一个新的trace信息。新构造的span是依据传入的参数进行构造。
     * @Param [spanName, traceId, spanId]
     * @return java.lang.String
     **/
    @GetMapping("/customSpanByTraceIdAndSpanId")
    @ResponseBody
    public String customSpanByTraceIdAndSpanId(String spanName,String traceId,String spanId){
        Assert.notNull(spanName, "spanName 不能为空");
        Assert.notNull(traceId, "traceId 不能为空");
        Assert.notNull(spanId, "spanId 不能为空");
        Context context =
                withSpanContext(
                        SpanContext.create(
                                traceId, spanId, TraceFlags.getSampled(), TraceState.getDefault()),
                        Context.current());
        Span span = tracer.spanBuilder(spanName)
                .setParent(context)
                .startSpan();
        span.setAttribute("attribute.a2", "some value");
        span.setAttribute("func","attr");
        span.setAttribute("app","otel3");
        span.end();
        return buildTraceUrl(span.getSpanContext().getTraceId());
    }

    private Context withSpanContext(SpanContext spanContext, Context context) {
        return context.with(Span.wrap(spanContext));
    }
}
