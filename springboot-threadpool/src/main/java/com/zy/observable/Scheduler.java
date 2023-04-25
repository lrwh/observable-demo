package com.zy.observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

import static com.zy.observable.ThreadPoolMonitor.threadPoolExecutor;


@Component
public class Scheduler {

    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    @Autowired
    public RestTemplate httpTemplate;
    @Value("${server.port}")
    public Integer port;

    @Scheduled(cron = "0/10 * * * * ?")
    public void exec(){
        //随机产生任务数
        Integer requestSize = new Random().nextInt(100);
        logger.info("requestSize:{}",requestSize);
        for (int i = 0; i < requestSize; i++) {
            threadPoolExecutor.execute(new BookTask(httpTemplate,port,i));
        }
    }


    public static class BookTask implements Runnable{
        private RestTemplate httpTemplate;
        private Integer port;
        private Integer currentReqIndex;
        public BookTask(RestTemplate httpTemplate,Integer port,Integer currentReqIndex){
            this.httpTemplate = httpTemplate;
            this.port = port;
            this.currentReqIndex = currentReqIndex;
        }
        @Override
        public void run() {

            String url =  String.format("http://localhost:%d/getBook?bookName=%s_%d",port,Thread.currentThread().getName(),currentReqIndex);
            logger.info("request url:{}",url);
            String body = httpTemplate.getForEntity(url, String.class).getBody();
            logger.info("response:{}"+body);
        }
    }
}
