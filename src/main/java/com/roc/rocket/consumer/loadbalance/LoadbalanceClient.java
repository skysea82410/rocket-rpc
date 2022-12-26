package com.roc.rocket.consumer.loadbalance;

import com.roc.rocket.consumer.Client;

import java.util.List;

/**
 * @author roc
 * @date 2022/12/2
 */
public interface LoadbalanceClient {

    public Client getClient(String api, List<Client> clients);
}
