package com.zy.observable.observabledemo.config;

//import io.jaegertracing.internal.MDCScopeManager;
import com.zy.observable.observabledemo.bean.MDCScopeManager;
import io.opentracing.contrib.java.spring.jaeger.starter.TracerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MDCScopeManagerConfig {

    /**
     * 推荐此种方式配置，这样可以使用JaegerAutoConfiguration进行自动配置
     *
     * @return
     */
    @Bean
    public TracerBuilderCustomizer mdcBuilderCustomizer() {
        // 1.8新特性，函数式接口
//        return builder -> builder.withScopeManager(new MDCScopeManager.Builder().build());
        return builder -> builder.withScopeManager(new MDCScopeManager());

    }
}