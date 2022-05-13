package com.zy.observable.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * @author liurui
 * @date 2022/05/11 14:32
 */
@CrossOrigin
@Controller
public class ServerController {
    @Autowired
    public RestTemplate httpTemplate;

    private static final Logger logger = LoggerFactory.getLogger(ServerController.class);

    @Value("${client:false}")
    private Boolean client;

    @Value("${api.url}")
    public String apiUrl;

    @Value("${extra.host}")
    public String clientHost;
    @Value("${server.port}")
    public String clientPort;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/gateway")
    @ResponseBody
    public String gateway(String tag) {
        logger.info("this is tag");
        sleep();
        httpTemplate.getForEntity(apiUrl + "/resource", String.class).getBody();
        httpTemplate.getForEntity(apiUrl + "/auth", String.class).getBody();
        if (client) {
            httpTemplate.getForEntity("http://" + clientHost + ":8081/client", String.class).getBody();
        }
        return httpTemplate.getForEntity(apiUrl + "/billing?tag=" + tag, String.class).getBody();
    }

    @GetMapping("/resource")
    @ResponseBody
    public String resource() {
        logger.info("this is resource");
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
//        try {
//            Thread.sleep(3000L);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
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
}
