package com.roc.rocket.provider.registry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.roc.common.utils.CollectionUtils;
import com.roc.rocket.provider.RocketProviderDefinition;
import com.roc.rocket.provider.config.reader.RocketProviderResouceReader;
import com.roc.rocket.registry.ProviderRegistryExecutor;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author roc
 * @date 2022/11/9
 */
public abstract class RocketProviderRegistry {

    private static Map<String, RocketProviderDefinition> rocketProducerDefinitionMap = Maps.newConcurrentMap();

    @Resource
    private RocketProviderResouceReader rocketProviderResouceReader;

    @Resource
    private ProviderRegistryExecutor providerRegistryExecutor;

    @PostConstruct
    private void init() {
        //读取Provider配置信息
        List<RocketProviderDefinition> producerDefinitionList = rocketProviderResouceReader.read();
        //把Provider信息放入到缓存中
        if (CollectionUtils.isNotEmpty(producerDefinitionList)) {
            for (RocketProviderDefinition rocketProviderDefinition : producerDefinitionList) {
                rocketProducerDefinitionMap.putIfAbsent(rocketProviderDefinition.getApiName(), rocketProviderDefinition);
            }
            //注册Provider信息到信息中心
            providerRegistryExecutor.register(Lists.newArrayList(rocketProducerDefinitionMap.values()));
        }
    }

    public RocketProviderDefinition getRocketProvider(String apiName) {
        return rocketProducerDefinitionMap.get(apiName);
    }

}
