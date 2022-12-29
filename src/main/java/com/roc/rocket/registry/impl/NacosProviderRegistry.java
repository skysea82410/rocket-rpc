package com.roc.rocket.registry.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.common.collect.Lists;
import com.roc.common.utils.CollectionUtils;
import com.roc.rocket.registry.IProviderRegistry;
import com.roc.rocket.registry.nacos.NacosService;
import com.roc.rocket.registry.provider.Provider;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author roc
 * @date 2022/12/27
 */
@Component
public class NacosProviderRegistry implements IProviderRegistry {

    @Resource
    private NacosService nacosService;

    @Override
    public void register(Provider provider) {
        try {
            nacosService.addInstance(provider);
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(Provider provider) {
        try {
            nacosService.removeInstance(provider);
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Provider> getProvider(String api) {
        try {
            List<Instance> instanceList = nacosService.getInstances(api, true);
            if (CollectionUtils.isEmpty(instanceList)) {
                return null;
            }
            List<Provider> providerList = Lists.newArrayList();
            for (Instance instance : instanceList) {
                Provider provider = new Provider();
                provider.setApi(instance.getServiceName());
                provider.setProvider(instance.getClusterName());
                provider.setIp(instance.getIp());
                provider.setPort(instance.getPort());
                providerList.add(provider);
            }
            return providerList;
        } catch (NacosException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Provider> getProviders() {
        return null;
    }
}
