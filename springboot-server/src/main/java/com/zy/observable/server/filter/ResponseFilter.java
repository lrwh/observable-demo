package com.zy.observable.server.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

@Component
public class ResponseFilter implements Filter {
    private  static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse)response;
        for (String headerName : httpServletResponse.getHeaderNames()) {
            logger.info("response header:{},value:{}",headerName,httpServletResponse.getHeader(headerName));
        }
        filterChain.doFilter(request,httpServletResponse);
    }

    @Override
    public void init(FilterConfig arg0)
            throws ServletException {

    }

    @Override
    public void destroy() {

    }

}