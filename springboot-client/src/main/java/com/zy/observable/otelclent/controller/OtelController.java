package com.zy.observable.otelclent.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author liurui
 * @date 2021/12/29 17:42
 */
@Controller
public class OtelController {

    private static final Logger logger = LoggerFactory.getLogger(OtelController.class);


    @GetMapping("/client")
    @ResponseBody
    public String client() {
        logger.info("this is 客户端");
        return "this is 客户端";
    }
}
