package com.zy.observable.server;

import com.zy.observable.server.bean.MyMBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import javax.management.*;
import java.lang.management.*;

@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            MyMBean myMBean = new MyMBean(0);
            ObjectName name = new ObjectName("com.zy.observable.server.bean:type=MyMBean");
            mbs.registerMBean(myMBean, name);
        }catch (Exception e){
            e.printStackTrace();
        }

        SpringApplication.run(ServerApplication.class, args);
    }

    @Bean
    public RestTemplate httpTemplate(){
        return new RestTemplate();
    }

}
