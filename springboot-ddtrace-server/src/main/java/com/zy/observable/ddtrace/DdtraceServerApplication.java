package com.zy.observable.ddtrace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class DdtraceServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DdtraceServerApplication.class, args);
    }



    @Bean
    public RestTemplate httpTemplate(){
        return new RestTemplate();
    }
}
