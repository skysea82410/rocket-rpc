package com.roc.rocket.registry.impl;

import com.google.common.collect.Lists;
import com.roc.common.utils.CollectionUtils;
import com.roc.common.utils.json.JsonUtils;
import com.roc.rocket.registry.IProviderRegistry;
import com.roc.rocket.registry.provider.Provider;
import com.roc.rocket.registry.provider.ProviderGroup;
import com.roc.rocket.utils.RedisUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author roc
 * @date 2022/11/9
 */
@Component
public class DefaultProviderRegistry implements IProviderRegistry {

    private static final Integer WRITER = 1;

    private static final String PROVIDER = "rocket_providers";

    @Resource
    private RedisUtils redisUtils;

    @Override
    public void register(Provider provider) {
        synchronized (WRITER) {
            Boolean isNotExist = true;
            List<ProviderGroup> providerGroupRegistry = getProviderGroupFromRedis();
            for (ProviderGroup providerGroup : providerGroupRegistry) {
                if (providerGroup.getApi().equals(provider.getApi())) {
                    if (!containProvider(provider, providerGroup.getProviderList())) {
                        providerGroup.getProviderList().add(provider);
                    }
                    isNotExist = false;
                    break;
                }
            }
            if (isNotExist) {
                ProviderGroup providerGroup = new ProviderGroup();
                providerGroup.setApi(provider.getApi());
                providerGroup.setProviderList(Lists.newArrayList(provider));
                providerGroupRegistry.add(providerGroup);
            }
            redisUtils.setValue(PROVIDER, JsonUtils.toJson(providerGroupRegistry));
        }
    }

    @Override
    public void remove(Provider provider) {
        synchronized (WRITER) {
            List<ProviderGroup> providerGroupRegistry = getProviderGroupFromRedis();
            for (ProviderGroup providerGroup : providerGroupRegistry) {
                if (providerGroup.getApi().equals(provider)) {
                    List<Provider> providers = providerGroup.getProviderList();
                    for (Provider p : providers) {
                        if (p.getIp().equals(provider.getProvider())
                                && p.getPort().equals(provider.getPort())) {
                            providers.remove(p);
                            redisUtils.setValue(PROVIDER, JsonUtils.toJson(providerGroupRegistry));
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<Provider> getProvider(String api) {
        List<ProviderGroup> providerGroups = getProviderGroupFromRedis();
        if (CollectionUtils.isNotEmpty(providerGroups)) {
            for (ProviderGroup providerGroup : providerGroups) {
                if (providerGroup.getApi().equals(api)) {
                    return providerGroup.getProviderList();
                }
            }
        }
        return null;
    }

    @Override
    public List<Provider> getProviders() {
        return null;
    }

    private List<ProviderGroup> getProviderGroupFromRedis() {
        List<ProviderGroup> providerGroupList = JsonUtils.fromJsonToList(redisUtils.getValue(PROVIDER), ProviderGroup.class);
        return providerGroupList == null ? Lists.newArrayList() : providerGroupList;
    }

    private Boolean containProvider(Provider provider, List<Provider> providerList) {
        if (CollectionUtils.isNotEmpty(providerList)) {
            for (Provider p : providerList) {
                if (provider.getIp().equals(p.getIp())
                        && provider.getPort().equals(p.getPort())) {
                    return true;
                }
            }
        }
        return false;
    }

}
