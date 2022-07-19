package com.zy.observable.ddtrace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;

/**
 * @author liurui
 * @date 2022/6/16 11:12
 */
@Component
public class CalcFilter implements Filter {
    Logger logger = LoggerFactory.getLogger(CalcFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        //请求时的系统时间
        LocalTime time1 = LocalTime.now();

        logger.info("START {}",request.getRequestURI() );
        filterChain.doFilter(servletRequest, servletResponse);
        //响应时的系统时间
        LocalTime time2 = LocalTime.now();
        //计算请求响应耗时
        Duration total = Duration.between(time1, time2);
        logger.info("END : {}",request.getRequestURI() + "耗时：" + total.toMillis());
    }
}
