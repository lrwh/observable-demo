package com.zy.observable.ddtrace.controller;

import com.zy.observable.ddtrace.util.ConstantsUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author liurui
 * @date 2022/7/18 15:22
 */
@Component
public abstract class BaseController {

    @GetMapping("/counter")
    @ResponseBody
    public Integer counter() {
        int get = ConstantsUtils.COUNTER.addAndGet(1);
        return get;
    }

}
