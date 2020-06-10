package com.xiezy.medium;

import com.xiezy.annotation.RemoteServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InitMedium implements BeanPostProcessor {

    private static ConcurrentHashMap<String, Class> aopBeans;

    /**
     * bean初始化后
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Class<?> clzss = bean.getClass();
        if (clzss.isAnnotationPresent(RemoteServer.class)) {
            Method[] methods = clzss.getInterfaces()[0].getMethods();
            String preStr = clzss.getInterfaces()[0].getName().replaceFirst("Impl", "") + ".";

            for (Method method : methods) {
                String methodName = method.getName();
                String wholeMethod = preStr + methodName;

                BeanMethod beanMethod = new BeanMethod();
                beanMethod.setMethod(method);
                beanMethod.setTarget(bean);

                Media.beanMethodMap.put(wholeMethod, beanMethod);
            }
        }

        return bean;
    }
}
