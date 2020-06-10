package com.xiezy.rpc;

import java.util.concurrent.atomic.AtomicLong;

public class Request {

    private Long id;

    private Object[] args;

    private String method;

    private static AtomicLong acl = new AtomicLong(1L);

    public Request() {
        id = acl.getAndIncrement();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
