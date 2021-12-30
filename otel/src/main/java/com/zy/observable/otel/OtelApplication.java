package com.zy.observable.otel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class OtelApplication {

    public static void main(String[] args) {
        SpringApplication.run(OtelApplication.class, args);
    }



    @Bean
    public RestTemplate httpTemplate(){
        return new RestTemplate();
    }
}
