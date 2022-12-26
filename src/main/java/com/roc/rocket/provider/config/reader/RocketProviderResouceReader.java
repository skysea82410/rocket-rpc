package com.roc.rocket.provider.config.reader;

import com.roc.rocket.provider.RocketProviderDefinition;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * @author roc
 * @date 2022/11/8
 */
@Component
public class RocketProviderResouceReader {

    private IRocketProviderResource rocketProviderResource;

    @Resource
    private DefaultProviderResourceReader defaultProviderResourceReader;

    @PostConstruct
    private void init() {
        if (rocketProviderResource == null) {
            rocketProviderResource = defaultProviderResourceReader;
        }
    }

    public List<RocketProviderDefinition> read() {
        return rocketProviderResource.read();
    }


}
