package com.roc.rocket.consumer.api.reader;

import com.google.common.collect.Lists;
import com.roc.common.utils.CollectionUtils;
import com.roc.rocket.consumer.api.config.RocketConsumerRootXmlConfig;
import com.roc.rocket.consumer.api.config.RocketConsumerXmlConfig;
import com.roc.rocket.consumer.def.RocketConsumerDefinition;
import com.roc.rocket.utils.XmlUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author roc
 * @date 2022/11/21
 */
public class DefaultRocketConsumerResourceReader implements IRocketConsumerResourceReader {

    private static final String RESOURCE = "rocket-consumer.xml";

    private static final String TYPE = "consumer";

    @Override
    public List<RocketConsumerDefinition> read() {
        RocketConsumerRootXmlConfig rocketConsumerRootXmlConfig = readXml();
        if (rocketConsumerRootXmlConfig == null
                || CollectionUtils.isEmpty(rocketConsumerRootXmlConfig.getRocketConsumerXmlConfigList())) {
            return null;
        }
        List<RocketConsumerDefinition> rocketConsumerDefinitionList = Lists.newArrayList();
        for (RocketConsumerXmlConfig rocketConsumerXmlConfig : rocketConsumerRootXmlConfig.getRocketConsumerXmlConfigList()) {
            if (TYPE.equals(rocketConsumerXmlConfig.getType())) {
                RocketConsumerDefinition rocketConsumerDefinition = new RocketConsumerDefinition();
                rocketConsumerDefinition.setId(rocketConsumerXmlConfig.getId());
                rocketConsumerDefinition.setApi(rocketConsumerXmlConfig.getApi());
                rocketConsumerDefinitionList.add(rocketConsumerDefinition);
            }
        }
        return rocketConsumerDefinitionList;
    }

    private RocketConsumerRootXmlConfig readXml() {
        Resource resource = new ClassPathResource(RESOURCE);
        BufferedReader br = null;
        RocketConsumerRootXmlConfig rocketConsumerRootXmlConfig = null;
        try {
            br = new BufferedReader(new InputStreamReader(resource.getInputStream(), "UTF-8"));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
            rocketConsumerRootXmlConfig = XmlUtils.xmlToObject(RocketConsumerRootXmlConfig.class, buffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return rocketConsumerRootXmlConfig;
    }
}
