package com.xiezy.proxy;

import com.alibaba.fastjson.JSONObject;
import com.xiezy.annotation.RemoteInvoke;
import com.xiezy.exception.RPCException;
import com.xiezy.rpc.Request;
import com.xiezy.rpc.Response;
import com.xiezy.netty.NettyClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 对使用了RemoteInvoke注解的属性进行动态代理，
 * 使其通过netty调用服务提供方的接口
 */
@Component
public class RemoteProxy implements BeanPostProcessor {

    /**
     * 实例化前对有RemoteInvoke注解的属性进行代理
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        Class<?> clzss = bean.getClass();
        //可以获取private字段，getFields只能获取public
        Field[] fields = clzss.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(RemoteInvoke.class)) {
                /**
                 * 保存方法对应的Class
                 */
                final Map<Method, Class> methodClassMap = new HashMap<Method, Class>();
                initMethodClassMap(methodClassMap, field);

                field.setAccessible(true);
                Enhancer enhancer = new Enhancer();
                enhancer.setInterfaces(new Class[]{field.getType()});
                enhancer.setCallback(new MethodInterceptor() {
                    @Override
                    public Object intercept(Object instance, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

                        Request request = new Request();
                        request.setArgs(args);
                        request.setMethod(methodClassMap.get(method).getName() + "." + method.getName());
                        Response response = NettyClient.send(request);
                        //异常
                        if (response.getId().compareTo(0L) <= 0) {
                            throw new RPCException(response.getContext().toString());
                        }

                        return JSONObject.toJavaObject((JSONObject)response.getContext(), method.getReturnType());
                    }
                });
                try {
                    field.set(bean, enhancer.create());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }

        return bean;
    }

    private void initMethodClassMap(Map<Method, Class> methodClassMap, Field field) {
        Method[] methods = field.getType().getMethods();
        for (Method method : methods) {
            methodClassMap.put(method, field.getType());
        }
    }
}
