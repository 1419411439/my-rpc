package com.xiezy.service.impl;

import com.xiezy.annotation.RemoteServer;
import com.xiezy.entity.User;
import com.xiezy.service.TestService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RemoteServer
public class TestServiceImpl implements TestService {

    @Value("${server.port}")
    private String serverPort;

    @Override
    public User add(User user) {

        System.out.println(user.getId() + " : " + user.getUserName());
        user.setUserName("服务端处理过--" + user.getUserName());

        return user;
    }

    @Override
    public Object sayHello() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("1", "Hello");
        map.put("2", "World");
        map.put("port", serverPort);

        return map;
    }

    @Override
    public Object argsTest(String s1, String s2, String s3, List<Object> l4) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("1", s1);
        map.put("2", s2);
        map.put("3", s3);
        map.put("4", l4);

        return map;
    }
}
