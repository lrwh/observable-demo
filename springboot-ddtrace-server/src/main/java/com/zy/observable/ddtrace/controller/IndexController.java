package com.zy.observable.ddtrace.controller;

import com.zy.observable.ddtrace.service.TestService;
import com.zy.observable.ddtrace.util.ConstantsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * @author liurui
 * @date 2021/12/29 17:42
 */
@Controller
public class IndexController {

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


    @GetMapping("/counter")
    @ResponseBody
    public Integer counter(){
        int get = ConstantsUtils.COUNTER.addAndGet(1);
        logger.info("counter:{}",get);
        return get;
    }
}
