package com.zy.observable.ddtrace.controller;

import com.zy.observable.ddtrace.service.TestService;
import com.zy.observable.ddtrace.util.ConstantsUtils;
import com.zy.observable.ddtrace.util.InheritableThreadLocalUtil;
import datadog.trace.api.DDTraceId;
import datadog.trace.api.IdGenerationStrategy;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.util.GlobalTracer;
import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author liurui
 * @date 2021/12/29 17:42
 */
@Controller
public class IndexController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Value("${client:false}")
    private Boolean client;
    @Autowired
    public RestTemplate httpTemplate;
    @Value("${api.url}")
    public String apiUrl;
    @Value("${extra.host}")
    public String extraHost;
    @Autowired
    private TestService testService;
    @Value("${sleep:0}")
    public Long sleep;

    @Value("${injectSwitch:true}")
    public Boolean injectSwitch;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/gateway")
    @ResponseBody
    public String gateway(String tag) {
        String userId = "user-" + System.currentTimeMillis();
        MDC.put(ConstantsUtils.MDC_USER_ID, userId);
        logger.info("this is tag");
        sleep();
        testService.getDemo();
        testService.apiTrace();
        if (injectSwitch) {
            inject();
        }
        httpTemplate.getForEntity(apiUrl + "/resource", String.class).getBody();

        InheritableThreadLocalUtil.setValue(GlobalTracer.get().activeSpan());

        logger.info("current traceiD:{}",InheritableThreadLocalUtil.getValue().context().toTraceId());


        new Thread(()->{
            logger.info("thread:{}",InheritableThreadLocalUtil.getValue().context().toTraceId());
            Span span = null;
            try {
                Tracer tracer = GlobalTracer.get();
                span = tracer.buildSpan("thread")
                        .asChildOf(InheritableThreadLocalUtil.getValue())
                        .start();
                span.setTag("threadName", Thread.currentThread().getName());
                logger.info("thread:{}",span.context().toTraceId());
                GlobalTracer.get().activateSpan(span);
                httpTemplate.getForEntity(apiUrl + "/auth", String.class).getBody();
            }finally {
                if (span!=null) {
                    span.finish();
                }
            }
        }).start();

        try {
            if (client) {
                httpTemplate.getForEntity("http://" + extraHost + ":8081/client", String.class).getBody();
            }
        } catch (Exception e) {
            buildErrorTrace(e);
        }
        InheritableThreadLocalUtil.remove();
        return httpTemplate.getForEntity(apiUrl + "/billing?tag=" + tag, String.class).getBody();
    }

    void inject() {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("test-inject").start();
        try (Scope scope = tracer.activateSpan(span)) {
            Map<String, String> data = new HashMap<>();
            data.put("d_url", "http://localhost/get");
            data.put("d_username", "liurui");

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("d_url", "http://localhost/get");
            httpHeaders.add("d_username", "liurui");
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(null, httpHeaders);

            tracer.inject(span.context(), Format.Builtin.TEXT_MAP,
                    new TextMapAdapter(data));
        } finally {
            span.finish();
        }

    }

    /***
     * 自定义traceId相关信息，实现自定义链路
     * @param traceId
     * @param parentId
     * @param treeLength
     * @return
     */
    @GetMapping("/customTrace")
    @ResponseBody
    public String customTrace(String traceId, String parentId, Integer treeLength) {
        //x-datadog-trace-id			123456789
        //x-datadog-parent-id			3784523368376145472
        // curl http://localhost:8086/customTrace?traceId=123456789&parentId=3784523368376145472
        Tracer tracer = GlobalTracer.get();
        traceId = StringUtils.isEmpty(traceId) ? IdGenerationStrategy.fromName("RANDOM").toString() : traceId;
        parentId = StringUtils.isEmpty(parentId) ? DDTraceId.ZERO.toString() : parentId;
        treeLength = treeLength == null ? 3 : treeLength;
        System.out.println(traceId + "\t" + parentId);
        for (int i = 0; i < treeLength; i++) {
            Map<String, String> data = new HashMap<>();
//            data.put("X-B3-TraceId", traceId);
//            data.put("X-B3-SpanId", parentId);
//            data.put("traceparent","00-1234567-9876543-01");
            data.put("x-datadog-trace-id", traceId);
            data.put("x-datadog-parent-id", parentId);
            SpanContext extractedContext = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapAdapter(data));
            Span serverSpan = tracer.buildSpan("opt" + i)
                    .withTag("service_name", "someService" + i)
                    .asChildOf(extractedContext)
                    .start();
            Scope scope = tracer.activateSpan(serverSpan);

//            tracer.inject(serverSpan.context(), Format.Builtin.TEXT_MAP,
//                    new TextMapAdapter(data));
            // 业务代码
            buildSpan();
            httpTemplate.getForEntity(apiUrl + "/billing?tag=customTrace" , String.class).getBody();
            scope.close();
            serverSpan.finish();
            parentId = serverSpan.context().toSpanId();
            System.out.println(traceId + "\t" + serverSpan.context().toTraceId() + "\t" + parentId);


        }
        return "build success!";
    }
    @GetMapping("/resource")
    @ResponseBody
    public String resource() {
        return "this is resource";
    }

    @GetMapping("/auth")
    @ResponseBody
    public String auth() {
        logger.info("this is auth");
        sleep();
        return "this is auth";
    }

    @GetMapping("/billing")
    @ResponseBody
    public String billing(String tag) {
        logger.info("this is method3,{}", tag);
        sleep();
        if (Optional.ofNullable(tag).get().equalsIgnoreCase("error")) {
            System.out.println(1 / 0);
        }
        return "下单成功";
    }

    private void sleep() {
        if (sleep > 0L) {
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping("/sleep")
    @ResponseBody
    public String setSleep(Long sleep) {
        this.sleep = sleep;
        return "休眠[" + sleep + " ms ]时间设置成功";
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

    private String result() {
        return client ? "【已开启】客户端请求" : "【已关闭】客户端请求";
    }

    @RequestMapping("/errorTrace")
    @ResponseBody
    public String errorTrace(String sign){
        try {
            System.out.println(0 / 0);
        }catch (Exception e){
            if (StringUtils.isNotBlank(sign) && sign.equalsIgnoreCase("trace")){
                buildErrorTrace(e);
            }else{
                throw e;
            }

        }
        return "200";
    }

    @RequestMapping("/buildSpan")
    @ResponseBody
    public String buildSpan(){
        // 获取一个 tracer 对象
        Tracer tracer = GlobalTracer.get();
        // 获取当前 span
        final Span span = GlobalTracer.get().activeSpan();
        System.out.println(span.context().toTraceId());
        // 创建三个子span
        for (int i = 0; i <3 ; i++) {
            Span serverSpan = tracer.buildSpan("spanName") // 配置span的名称
                    .withTag("service_name", "someService" + i) // 配置span tag
                    .asChildOf(span) // 设置span的 父span
                    .start(); // 开启一个span
            tracer.activateSpan(serverSpan).close();
            serverSpan.finish();
        }
        return "buildSpan success";
    }

    /**
     * 调整API 接口
     * @param apiUrl
     * @return
     */
    @GetMapping("/setApi")
    @ResponseBody
    public String setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
        return apiUrl;
    }

    @RequestMapping("/thread")
    @ResponseBody
    public String threadTest(){
        logger.info("this func is threadTest.");
        InheritableThreadLocalUtil.setValue(GlobalTracer.get().activeSpan());
        logger.info("current traceId:{}",GlobalTracer.get().activeSpan().context().toTraceId());

        new Thread(()->{
            Span span = null;
            try {
                Tracer tracer = GlobalTracer.get();
                span = tracer.buildSpan("thread")
                        .asChildOf(InheritableThreadLocalUtil.getValue())
                        .start();
                span.setTag("threadName", Thread.currentThread().getName());
                GlobalTracer.get().activateSpan(span);
                logger.info("this is new Thread.");
                logger.info("new Thread get span:{}",InheritableThreadLocalUtil.getValue());
                logger.info("thread:{}",span.context().toTraceId());
            }finally {
                if (span!=null) {
                    span.finish();
                    logger.info("span finish.");
                }
                InheritableThreadLocalUtil.remove();
            }
        }).start();
        return "success";
    }

    @RequestMapping("/execThread")
    @ResponseBody
    public String ExecutorServiceTest(){
        ExecutorService executor = Executors.newCachedThreadPool();
        logger.info("this func is ExecutorServiceTest.");
        executor.submit(()->{
            logger.info("this is executor Thread.");
        });
        return "ExecutorService";
    }
}