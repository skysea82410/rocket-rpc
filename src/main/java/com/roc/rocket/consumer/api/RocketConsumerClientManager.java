package com.roc.rocket.consumer.api;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.roc.common.utils.CollectionUtils;
import com.roc.rocket.consumer.Client;
import com.roc.rocket.consumer.api.reader.DefaultRocketConsumerResourceReader;
import com.roc.rocket.consumer.api.reader.RocketConsumerResouceReader;
import com.roc.rocket.consumer.def.RocketConsumerDefinition;
import com.roc.rocket.consumer.loadbalance.LoadbalanceClient;
import com.roc.rocket.consumer.loadbalance.RoundRobin;
import com.roc.rocket.exception.NoSuchProviderException;
import com.roc.rocket.registry.ProviderRegistryExecutor;
import com.roc.rocket.registry.provider.Provider;
import com.roc.rocket.utils.ApplicationContextUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author roc
 * @date 2022/11/10
 */
@Component
public class RocketConsumerClientManager {

    private static final Map<String, List<Client>> consumerClientMap = Maps.newConcurrentMap();

    @Resource
    private ProviderRegistryExecutor providerRegistryExecutor;

    private LoadbalanceClient loadbalanceClient;

    public RocketConsumerClientManager() {
    }

    public RocketConsumerClientManager(LoadbalanceClient loadbalanceClient) {
        this.loadbalanceClient = loadbalanceClient;
    }


    @PostConstruct
    private void init() {
        initLoadbalance();
        initProviders();
    }

    public Client getConsumerClient(String api) {
        if (consumerClientMap.size() == 0) {
            initProviders();
        }
        List<Client> clientList = consumerClientMap.get(api);
        if (CollectionUtils.isEmpty(clientList)) {
            throw new NoSuchProviderException();
        }
        return loadbalanceClient.getClient(api, clientList);
    }


    private void initLoadbalance() {
        if (Objects.isNull(loadbalanceClient)) {
            loadbalanceClient = ApplicationContextUtils.getBean(RoundRobin.class);
        }
    }

    private void initProviders() {
        RocketConsumerResouceReader rocketConsumerResouceReader = new RocketConsumerResouceReader(new DefaultRocketConsumerResourceReader());
        //读取comsumer配置
        List<RocketConsumerDefinition> rocketConsumerDefinitionList = rocketConsumerResouceReader.read();
        if (CollectionUtils.isNotEmpty(rocketConsumerDefinitionList)) {
            Map<String, List<Client>> serverMap = Maps.newHashMap();
            for (RocketConsumerDefinition rocketConsumerDefinition : rocketConsumerDefinitionList) {
                //通过api匹配注册中心提供服务的provider
                List<Provider> providers = providerRegistryExecutor.getProvider(rocketConsumerDefinition.getApi());
                if (CollectionUtils.isNotEmpty(providers)) {
                    Provider provider = Iterables.getFirst(providers, null);
                    if (provider == null) {
                        continue;
                    }
                    //生成client，连接provider
                    if (!serverMap.containsKey(provider.getProvider())) {
                        List<Client> clients = providers.stream().map(p -> buildConsumerClient(p)).collect(Collectors.toList());
                        serverMap.put(provider.getProvider(), clients);
                    }
                    consumerClientMap.put(rocketConsumerDefinition.getApi(), serverMap.get(provider.getProvider()));
                }
            }
        }
    }

    private Client buildConsumerClient(Provider provider) {
        return new Client(provider.getIp(), provider.getPort());
    }
}
