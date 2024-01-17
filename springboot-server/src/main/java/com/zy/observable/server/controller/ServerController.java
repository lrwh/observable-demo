package com.zy.observable.server.controller;

import com.zy.observable.server.bean.AjaxResult;
import com.zy.observable.server.service.TestService;
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

    @Value("${sleep:0}")
    public Long sleep;

    @Autowired
    private TestService testService;

    @GetMapping("/user")
    @ResponseBody
    public String getUser(){
        logger.info("do getUser");
        return testService.users();
    }
    @GetMapping("/")
    public String index() {
        return "index";
    }

//    @GetMapping("/gateway")
    @RequestMapping("/gateway")
    @ResponseBody
    public AjaxResult gateway(String tag) {
        logger.info("this is tag");
        sleep();
        httpTemplate.getForEntity(apiUrl + "/resource", String.class).getBody();
        httpTemplate.getForEntity(apiUrl + "/auth", String.class).getBody();
        if (client) {
            try {
                httpTemplate.getForEntity("http://" + clientHost + ":8081/client", String.class).getBody();
            }catch (Exception e){
                return AjaxResult.error("client 调用失败");
            }
        }
        return httpTemplate.getForEntity(apiUrl + "/billing?tag=" + tag, AjaxResult.class).getBody();
    }

    @RequestMapping("/resource")
    @ResponseBody
    public AjaxResult resource() {
        logger.info("this is resource");
        return AjaxResult.success("this is resource");
    }

    @RequestMapping("/auth")
    @ResponseBody
    public AjaxResult auth() {
        logger.info("this is auth");
        sleep();
        return AjaxResult.success("this is auth");
    }

    @RequestMapping("/billing")
    @ResponseBody
    public AjaxResult billing(String tag) {
        logger.info("this is method3,{}", tag);
        sleep();
        if (Optional.ofNullable(tag).get().equalsIgnoreCase("error")) {
            System.out.println(1 / 0);
        }
        return AjaxResult.success("下单成功");
    }

    private void sleep() {
        if (sleep>0L) {
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping("/getClient")
    @ResponseBody
    public String getClient() {
        sleep();
        return result();
    }

    @RequestMapping("/setClient")
    @ResponseBody
    public String setClient(Boolean c) {
        client = c;
        return result();
    }

    @RequestMapping("/sleep")
    @ResponseBody
    public String setSleep(Long sleep){
        this.sleep = sleep;
        return "休眠["+sleep+" ms ]时间设置成功";
    }

    @GetMapping("/testError")
    @ResponseBody
    public AjaxResult error(){
        return new AjaxResult(400,"异常测试");
    }

    @GetMapping("/npe")
    @ResponseBody
    public AjaxResult npe(){
        String a = null;
        System.out.println(a.toString());
        return AjaxResult.success();
    }
    private String result() {
        return client ? "【已开启】客户端请求" : "【已关闭】客户端请求";
    }
}
