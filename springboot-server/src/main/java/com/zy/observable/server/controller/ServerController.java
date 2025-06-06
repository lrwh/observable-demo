package com.zy.observable.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.zy.observable.server.bean.AjaxResult;
import com.zy.observable.server.service.TestService;
import com.zy.observable.server.vo.Student;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        logger.info("do getUser,currentThread:{}",Thread.currentThread().getName());
        return testService.users();
    }
    @GetMapping("/")
    public String index() {
        return "index";
    }

//    @GetMapping("/gateway")
    @RequestMapping("/gateway")
    @ResponseBody
    public AjaxResult gateway(String tag,String name) {
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
        httpTemplate.getForEntity(apiUrl + "/billing?tag=" + tag, AjaxResult.class).getBody();
        return AjaxResult.success("支付成功");
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

    @PostMapping("/student")
    @ResponseBody
    public String addStudent(@RequestBody Student student){
        logger.info("add student:{}", JSONObject.toJSONString(student));
        return JSONObject.toJSONString(student);
    }
    @PostMapping("/jsonStr")
    @ResponseBody
    public String addJsonStr(@RequestBody String jsonStr){
        logger.info("add jsonStr:{}", jsonStr);
        return jsonStr;
    }

    @RequestMapping(value="/toJsonStr",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String toJsonStr(){
        return JSONObject.toJSONString(new Student("tom",20));
    }


    private String result() {
        return client ? "【已开启】客户端请求" : "【已关闭】客户端请求";
    }

    @RequestMapping("/download")
    public Object download(HttpServletRequest request, HttpServletResponse response) {
        logger.info("download file");

        final String fileName = "AI Agent 可观测性探索与实践（终版）.pptx";
        if (!StringUtils.isEmpty(fileName)) {
            // 下载
            final File file = new File("/home/liurui/驻云/aws", fileName);
            try {
                response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            response.setContentType("application/octet-stream");
            try (final ServletOutputStream outputStream = response.getOutputStream()){
                Files.copy(Paths.get(file.getPath()), outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
