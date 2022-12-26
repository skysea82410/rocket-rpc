package com.roc.rocket.consumer.api.reader;

import com.roc.rocket.consumer.def.RocketConsumerDefinition;

import java.util.List;

/**
 * @author roc
 * @date 2022/11/8
 */
public class RocketConsumerResouceReader {

    private IRocketConsumerResourceReader rocketConsumerResourceReader;


    public RocketConsumerResouceReader(IRocketConsumerResourceReader rocketConsumerResourceReader) {
        this.rocketConsumerResourceReader = rocketConsumerResourceReader;
    }


    public List<RocketConsumerDefinition> read() {
        return rocketConsumerResourceReader.read();
    }
}
