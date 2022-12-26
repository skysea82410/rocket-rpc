package com.roc.rocket.consumer.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Proxy;

/**
 * @author roc
 * @date 2022/10/31
 */
@Slf4j
@Component
public class RocketRpcProxy {

    @Resource
    private ApplicationContext applicationContext;

    public <T> T create(Class<?> clazz, String api) {
        RpcProxy rpcProxy = applicationContext.getBean(RpcProxy.class);
        rpcProxy.setApi(api);
        rpcProxy.setClazz(clazz);
        Class<?>[] interfaces = clazz.isInterface() ? new Class[]{clazz} : clazz.getInterfaces();
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, rpcProxy);
    }

}
