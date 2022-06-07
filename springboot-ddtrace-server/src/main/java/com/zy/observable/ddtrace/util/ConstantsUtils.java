package com.zy.observable.ddtrace.util;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liurui
 * @date 2021/12/28 11:56
 */
public class ConstantsUtils {
    public static final String MDC_USER_ID = "user-id";

    public static final AtomicInteger COUNTER = new AtomicInteger(0);
}
