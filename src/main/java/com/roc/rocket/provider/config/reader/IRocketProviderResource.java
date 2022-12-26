package com.roc.rocket.provider.config.reader;

import com.roc.rocket.provider.RocketProviderDefinition;

import java.util.List;

/**
 * @author roc
 * @date 2022/11/21
 */
public interface IRocketProviderResource {

    public List<RocketProviderDefinition> read();
}
