package com.zy.observable.observabledemo.controller;

import com.zy.observable.observabledemo.bean.TraceContext;
import com.zy.observable.observabledemo.util.ConstantsUtils;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.Random;

/**
 * @author liurui
 * @date 2021/12/27
 */
@Controller
public class JaegerController {
    private static final Logger logger = LoggerFactory.getLogger(JaegerController.class);

    @Value("${api.url}")
    private String apiUrl;
    @Autowired
    RestTemplate httpTemplate;
    @Value("${extra.host}")
    private String extraHost;
    @Value("${opentracing.jaeger.udp-sender.ui-port}")
    private String jaegerUiPort;
    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private Tracer tracer;

    @GetMapping("/")
    public String index(){
        return "index";
    }

    @GetMapping("/gateway")
    @ResponseBody
    public String gateway(String tag){
        String userId = "user-" + System.currentTimeMillis();
        TraceContext.setField(ConstantsUtils.MDC_USER_ID,userId);
        MDC.put(ConstantsUtils.MDC_USER_ID,userId);
        if (tracer!=null&&tracer.activeSpan()!=null && StringUtils.isNotBlank(tag)) {
            tracer.activeSpan().setTag("username", tag);
        }
        logger.info("this is tag");
        sleep();
        httpTemplate.getForEntity(apiUrl+"/resource",String.class).getBody();
        httpTemplate.getForEntity(apiUrl+"/auth",String.class).getBody();
        return httpTemplate.getForEntity(apiUrl+"/billing?tag="+tag,String.class).getBody();
    }

    @GetMapping("/resource")
    @ResponseBody
    public String resource(){
        return "this is resource";
    }

    @GetMapping("/auth")
    @ResponseBody
    public String auth(){
        logger.info("this is auth");
        sleep();
        return "this is auth";
    }

    @GetMapping("/billing")
    @ResponseBody
    public String billing(String tag){
        logger.info("this is method3,{}",tag);
        sleep();
        if (Optional.ofNullable(tag).get().equalsIgnoreCase("error")){
            System.out.println(1/0);
        }
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.activeSpan();
        return "<a href='http://"+extraHost+":"+serverPort+"/getTrace?traceId="+span.context().toTraceId().trim()+"'>"+span.context().toTraceId()+"</a>";
//        return "<a href='http://"+jaegerHost+":"+jaegerUiPort+"/trace/"+span.context().toTraceId().trim()+"'>"+span.context().toTraceId()+"</a>";

    }
    @GetMapping("/getTrace")
    public String getTrace(String traceId){
        return "redirect:http://"+extraHost+":"+jaegerUiPort+"/trace/"+traceId;
    }

    private void sleep(){
        Random random = new Random();
        int time = random.nextInt(1000);
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/createSpan")
    @ResponseBody
    public String createSpan(){
        logger.info("当前活动spanId，{}",tracer.activeSpan().context().toSpanId());
        // 自定义span
        Span span = tracer.buildSpan("mySpan").withTag("env","test").start();


        // 激活自定义span
        Scope scope = tracer.activateSpan(span);
        // 给span设置一个log event
        span.log("hello");
        // 设置一个tag
        span.setTag("serviceName","app1");
        // 设置 baggage event
        span.setBaggageItem("userId","user"+System.currentTimeMillis());
        span.setBaggageItem("username","user"+System.currentTimeMillis());

        logger.info("new spanId: {}",span.context().toSpanId());
        logger.info("当前活动spanId,{}",tracer.activeSpan().context().toSpanId());

        try (Scope scope1 = tracer.activateSpan(span)) {
            sonSpan();
        } finally {
            // 完成span
            span.finish();
        }
        // scope关闭
        scope.close();
        return tracer.activeSpan().context().toTraceId();
    }

    private void sonSpan(){
        Span sonSpan = tracer.buildSpan("sonSpan").start();
        sonSpan.finish();
    }

}
