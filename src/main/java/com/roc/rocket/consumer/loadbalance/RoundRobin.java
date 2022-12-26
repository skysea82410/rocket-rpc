package com.roc.rocket.consumer.loadbalance;

import com.google.common.collect.Maps;
import com.roc.rocket.consumer.Client;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author roc
 * @date 2022/12/2
 */
@Component
public class RoundRobin implements LoadbalanceClient {

    private static final Map<String, Integer> clientCallMap = Maps.newHashMap();

    @Override
    public Client getClient(String api, List<Client> clients) {
        synchronized (api) {
            Integer clientNums = clients.size();
            if (!clientCallMap.containsKey(api)) {
                clientCallMap.put(api, 0);
                return clients.get(0);
            }
            Integer index = clientCallMap.get(api) + 1;
            if (index >= clientNums) {
                index = 0;
            }
            clientCallMap.put(api, index);
            return clients.get(index);
        }
    }
}
