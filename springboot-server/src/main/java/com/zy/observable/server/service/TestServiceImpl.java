package com.zy.observable.server.service;

import com.zy.observable.server.vo.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TestServiceImpl implements TestService {
    private static final Logger logger = LoggerFactory.getLogger(TestServiceImpl.class);
    public String getUsername(){
        return "lr";
    }

    public String users(){
        Map<Integer, Student> users =new HashMap<>();
        users.put(1,new Student("tom",18));
        users.put(2,new Student("joy",20));
        users.put(3,new Student("lucy",30));
        users.forEach((k,v)->print(k,v));
        return getUsername();
    }

    public void print(Integer level,Student student){
        logger.info("level:{},username:{}",level,student.getUsername());
    }
}

