package com.xiezy.service;

import com.xiezy.entity.User;

import java.util.List;

public interface TestService {

    public User add(User user);

    public Object sayHello();

    public Object argsTest(String s1, String s2, String s3, List<Object> l4);
}
