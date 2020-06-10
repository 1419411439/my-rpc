package com.xiezy.rpc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultFuture {

    private static Map<Long, DefaultFuture> allDefaultFuture = new ConcurrentHashMap<Long, DefaultFuture>();

    private final Lock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    private Response response;

    //默认2分钟
    //private Long timeout = 2 * 60L;
    private Long timeout = 2 * 10L;

    private Long startTime = System.currentTimeMillis();

    public DefaultFuture(Request request, Long timeout) {
        this(request);
        this.timeout = timeout;
    }

    public DefaultFuture(Request request) {
        allDefaultFuture.put(request.getId(), this);
    }

    /**
     * 主线程阻塞等待返回结果
     * @return
     */
    public Response get() {
        lock.lock();
        try {
            while (!done()) {
                condition.await();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return this.response;
    }

    public Response getTimeout(Long timeout) {
        if (null == timeout) {
            timeout = this.timeout;
        }
        lock.lock();
        try {
            while (!done()) {
                condition.await(timeout, TimeUnit.SECONDS);
                //RPC调用超时
                if (System.currentTimeMillis() - this.startTime > timeout) {
                    Response response = new Response();
                    response.setContext("调用超时！！");
                    response.setId(-1L);

                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return this.response;
    }

    public static void recive(Response response) {
        DefaultFuture df = allDefaultFuture.get(response.getId());

        if (null != df) {
            Lock lock = df.lock;
            try {
                lock.lock();
                df.condition.signal();
                df.setResponse(response);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        allDefaultFuture.remove(df);
    }

    private boolean done() {
        return this.response != null;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

}
