package com.xiezy.controller;

import com.xiezy.annotation.RemoteInvoke;
import com.xiezy.entity.User;
import com.xiezy.service.TestService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;

@RestController
public class TestController {

    @RemoteInvoke
    private TestService testService;


    @RequestMapping("/test")
    public Object test() {
        User user = new User();
        user.setId(1L);
        user.setUserName("xxxxx");

        return testService.add(user);
    }

    @RequestMapping("/sayHello")
    public Object sayHello() {
        return testService.sayHello();
    }

    @RequestMapping("/argsTest")
    public Object argsTest() {
        return testService.argsTest("arg1", "arg2", "arg3", Arrays.asList("ll1", "ll2"));
    }

}
