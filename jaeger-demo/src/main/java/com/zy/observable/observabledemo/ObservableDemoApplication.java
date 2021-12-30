package com.zy.observable.observabledemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ObservableDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObservableDemoApplication.class, args);
    }

    @Bean
    public RestTemplate httpTemplate(){
        return new RestTemplate();
    }

}
