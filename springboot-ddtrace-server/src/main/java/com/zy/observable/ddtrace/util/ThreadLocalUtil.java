package com.zy.observable.ddtrace.util;

import io.opentracing.Span;

public class ThreadLocalUtil {

    public static final ThreadLocal<Span> THREAD_LOCAL = new ThreadLocal<>();

    //设置线程需要保存的值
    public static void setValue (Span str) {
        THREAD_LOCAL.set(str);
    }

    //获取线程中保存的值
    public static Span getValue() {
        return THREAD_LOCAL.get();
    }

    //移除线程中保存的值
    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
