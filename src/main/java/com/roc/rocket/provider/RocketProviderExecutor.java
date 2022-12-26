package com.roc.rocket.provider;

import com.roc.rocket.exception.NoSuchMethodException;
import com.roc.rocket.exception.NoSuchProviderException;
import com.roc.rocket.provider.registry.RocketProviderRegistry;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author roc
 * @date 2022/11/7
 */
@Component
public class RocketProviderExecutor extends RocketProviderRegistry {

    public Object call(String api, String methodName, Object[] values) throws Exception {
        //通过调用api获取Provider信息
        RocketProviderDefinition rocketProviderDefinition = getRocketProvider(api);
        if (rocketProviderDefinition == null) {
            throw new NoSuchProviderException();
        }
        Method[] methods = rocketProviderDefinition.getClazz().getMethods();
        //找到执行方法
        Method method = findMethod(methods, methodName);
        if (method == null) {
            throw new NoSuchMethodException();
        }
        //通过反射执行并返回结果
        return method.invoke(rocketProviderDefinition.getInstance(), values);
    }

    private Method findMethod(Method[] methods, String methodName) {
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        return null;
    }


}
