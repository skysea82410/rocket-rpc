package com.roc.rocket.registry;

import com.roc.rocket.registry.provider.Provider;

import java.util.List;

/**
 * @author roc
 * @date 2022/11/9
 */
public interface IProviderRegistry {

    void register(Provider provider);

    void remove(Provider provider);

    List<Provider> getProvider(String api);

    List<Provider> getProviders();
}
