package com.roc.rocket.consumer;

import com.roc.rocket.consumer.api.RocketConsumerClientManager;
import com.roc.rocket.protocol.RocketProtocol;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author roc
 * @date 2022/11/15
 */
@Component
public class RpcExecutor {

    @Resource
    private RocketConsumerClientManager rocketConsumerClientManager;

    public Object call(RocketProtocol rocketProtocol, String api) throws Exception {
        Client client = rocketConsumerClientManager.getConsumerClient(api);
        return client.call(rocketProtocol);
    }


}
