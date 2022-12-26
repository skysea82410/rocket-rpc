package com.roc.rocket.registry.provider;

import lombok.Data;

import java.util.List;

/**
 * @author roc
 * @date 2022/11/15
 */
@Data
public class ProviderGroup {

    private String api;

    private List<Provider> providerList;
}
