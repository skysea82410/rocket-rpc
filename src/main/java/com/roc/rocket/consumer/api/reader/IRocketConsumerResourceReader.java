package com.roc.rocket.consumer.api.reader;

import com.roc.rocket.consumer.def.RocketConsumerDefinition;

import java.util.List;

/**
 * @author roc
 * @date 2022/11/21
 */
public interface IRocketConsumerResourceReader {

    public List<RocketConsumerDefinition> read();
}
