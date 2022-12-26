package com.roc.rocket.consumer.proxy;

import lombok.Data;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author roc
 * @date 2022/11/10
 */
@Component
@Data
public class RocketRpcProxyFactory<T> implements FactoryBean<T> {

    private Class<?> clazz;

    private String api;

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public T getObject() throws Exception {
        RocketRpcProxy rocketRpcProxy = applicationContext.getBean(RocketRpcProxy.class);
        return rocketRpcProxy.create(clazz, api);
    }

    @Override
    public Class<?> getObjectType() {
        return clazz;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


}
