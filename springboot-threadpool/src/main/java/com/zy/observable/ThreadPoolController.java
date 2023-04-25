package com.zy.observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping
public class ThreadPoolController {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolController.class);

    @GetMapping("/getBook")
    public String getBook(String bookName){
        logger.info("do get request:{}",bookName);
        try {
            // 假设业务执行耗时
            Thread.sleep(new Random().nextInt(10000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("do get response:{}",bookName);
        return "bookName:"+bookName;
    }
}
