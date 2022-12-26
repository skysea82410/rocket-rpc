package com.roc.rocket.registry;

import com.roc.common.utils.CollectionUtils;
import com.roc.rocket.provider.RocketProviderDefinition;
import com.roc.rocket.registry.impl.DefaultProviderRegistry;
import com.roc.rocket.registry.provider.Provider;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * @author roc
 * @date 2022/11/10
 */
@Component
public class ProviderRegistryExecutor {

    private IProviderRegistry providerRegistry;

    @Resource
    private DefaultProviderRegistry defaultProviderRegistry;

    public ProviderRegistryExecutor() {
    }

    public ProviderRegistryExecutor(IProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
    }

    @PostConstruct
    private void init() {
        if (this.providerRegistry == null) {
            this.providerRegistry = defaultProviderRegistry;
        }
    }

    public void register(Provider provider) {
        providerRegistry.register(provider);
    }

    public void register(List<RocketProviderDefinition> rocketProviderDefinitionList) {
        if (CollectionUtils.isNotEmpty(rocketProviderDefinitionList)) {
            for (RocketProviderDefinition rocketProviderDefinition : rocketProviderDefinitionList) {
                register(buildProvider(rocketProviderDefinition));
            }
        }
    }

    public void remove(Provider provider) {
        providerRegistry.remove(provider);
    }

    public List<Provider> getProvider(String api) {
        return providerRegistry.getProvider(api);
    }

    public List<Provider> getProviders() {
        return providerRegistry.getProviders();
    }

    public void setProviderRegistry(IProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
    }

    private Provider buildProvider(RocketProviderDefinition rocketProviderDefinition) {
        Provider provider = new Provider();
        provider.setApi(rocketProviderDefinition.getApiName());
        provider.setProvider(rocketProviderDefinition.getProvider());
        provider.setIp(rocketProviderDefinition.getIp());
        provider.setPort(rocketProviderDefinition.getPort());
        return provider;
    }
}
