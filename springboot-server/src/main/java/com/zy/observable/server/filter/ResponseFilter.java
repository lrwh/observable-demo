//package com.zy.observable.server.filter;
//
//import com.alibaba.fastjson.JSON;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.*;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//@Component
//public class ResponseFilter implements Filter {
//    private  static final Logger logger = LoggerFactory.getLogger(ResponseFilter.class);
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
//            throws IOException, ServletException {
//        HttpServletResponse httpServletResponse = (HttpServletResponse)response;
//        for (String headerName : httpServletResponse.getHeaderNames()) {
//            logger.info("response header:{},value:{}",headerName,httpServletResponse.getHeader(headerName));
//        }
//        System.out.println(">>"+response.getContentType());
//        ResponseWrapper wrapper = new ResponseWrapper(httpServletResponse);
////        filterChain.doFilter(request,httpServletResponse);
//        try {
//            filterChain.doFilter(request,wrapper);
//            if (wrapper.getContent().equals("application/octet-stream")){
//                return;
//            }
//            System.out.println("lenth:"+wrapper.getContent().length);
//            if (wrapper.getContent().length>0) {
//                String responseStr = new String(wrapper.getContent(), "UTF-8");
//                Object parse = JSON.parse(responseStr);
//
////            AjaxResult ajaxResult = new AjaxResult(parse);
////            baseResult.setData(parse);
////            if (parse instanceof JSONObject){
////                JSONObject resultObject = (JSONObject) parse;
////                if (resultObject.containsKey("status")&&resultObject.containsKey("message")&&resultObject.containsKey("data")){
////                    baseResult = JSONObject.parseObject(resultObject.toJSONString(),new TypeReference<BaseResult<Object>>(){});
////                }
////            }
////            LOGGER.info("response is ============{}",baseResult);
//
//                System.out.println(">>>>>>>>" + JSON.toJSONBytes(parse));
//                //必须设置ContentLength
//                response.setContentLength(JSON.toJSONBytes(parse).length);
//                //根据http accept来设置，我这里为了简便直接写json了
//                response.setContentType("application/json;charset=utf-8");
//                response.getOutputStream().write(JSON.toJSONBytes(parse));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    @Override
//    public void init(FilterConfig arg0)
//            throws ServletException {
//
//    }
//
//    @Override
//    public void destroy() {
//
//    }
//
//}
