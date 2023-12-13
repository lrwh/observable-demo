package com.zy.observable.server.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * @author liurui
 * @date 2022/5/11 11:24
 */
@Component
public class CorsFilter implements Filter {

    private  static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        HttpServletRequest request = (HttpServletRequest)servletRequest;

        Enumeration<String> headerNames = request.getHeaderNames();
        StringBuffer sb = new StringBuffer("\n");
        while(headerNames.hasMoreElements()){
            String headerName = headerNames.nextElement();
            sb.append(headerName+"\t\t\t\t:"+request.getHeader(headerName)).append("\n");
        }
        logger.info("url:{},header:{}",request.getRequestURI(),sb.toString());
        filterChain.doFilter(request,response);
    }
}
