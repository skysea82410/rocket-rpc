package com.roc.rocket.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.roc.rocket.registry.provider.Provider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author roc
 * @date 2022/12/27
 */
@Component
public class NacosService {

    @Value("${roc.rocket.nacos.server}")
    private String serverAddress;

    private NamingService namingService;

    public void addInstance(Provider provider) throws NacosException {
        Instance instance = new Instance();
        instance.setIp(provider.getIp());
        instance.setPort(provider.getPort());
        instance.setHealthy(true);
        instance.setClusterName(provider.getProvider());
        getNamingService().registerInstance(provider.getApi(), instance);
    }

    public void removeInstance(Provider provider) throws NacosException {
        getNamingService().deregisterInstance(provider.getApi(), provider.getIp(), provider.getPort());
    }

    public List<Instance> getInstances(String api, Boolean healthy) throws NacosException {
        return getNamingService().getAllInstances(api, healthy);
    }

    private NamingService getNamingService() throws NacosException {
        if (namingService == null) {
            namingService = NamingFactory.createNamingService(serverAddress);
        }
        return namingService;
    }
}
