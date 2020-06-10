package com.xiezy.medium;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

@Getter
@Setter
public class BeanMethod {

    private Method method;

    private Object target;

    private String[] args;

    public Object process() {
        Object result = null;

        try {
//            Class<?> parameterType = method.getParameterTypes()[0];
//            Object param = JSONObject.parseObject(args, parameterType);
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] targetArgs = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                targetArgs[i] = JSONObject.parseObject(args[i], parameterTypes[i]);
            }

            result = method.invoke(target, targetArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
