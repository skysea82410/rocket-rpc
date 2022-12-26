package com.roc.rocket.provider.config.reader;

import com.google.common.collect.Lists;
import com.roc.common.utils.CollectionUtils;
import com.roc.rocket.provider.RocketProviderDefinition;
import com.roc.rocket.provider.config.xml.RocketProviderRootXmlConfig;
import com.roc.rocket.provider.config.xml.RocketProviderXmlConfig;
import com.roc.rocket.utils.XmlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author roc
 * @date 2022/11/21
 */
@Component
public class DefaultProviderResourceReader implements IRocketProviderResource {

    /**
     * 默认的provider配置文件名
     */
    private static final String RESOURCE = "rocket-producer.xml";

    /**
     * 默认的类型
     */
    private static final String TYPE = "provider";

    /**
     * provider所在的项目名
     */
    @Value("${roc.rocket.server.provider}")
    private String provider;

    /**
     * 提供服务的端口
     */
    @Value("${roc.rocket.server.port}")
    private Integer port;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public List<RocketProviderDefinition> read() {
        //读取XML配置内容
        RocketProviderRootXmlConfig rocketProviderRootXmlConfig = readXml();
        if (rocketProviderRootXmlConfig == null
                || CollectionUtils.isEmpty(rocketProviderRootXmlConfig.getRocketProviderXmlConfigList())) {
            return null;
        }
        List<RocketProviderDefinition> rocketProviderDefinitionList = Lists.newArrayList();
        //解析XML配置内容，把XML配置的内容，转化为RocketProviderDefinition对象
        for (RocketProviderXmlConfig rocketProviderXmlConfig : rocketProviderRootXmlConfig.getRocketProviderXmlConfigList()) {
            if (TYPE.equals(rocketProviderXmlConfig.getType())) {
                RocketProviderDefinition rocketProviderDefinition = new RocketProviderDefinition();
                rocketProviderDefinition.setApiName(rocketProviderXmlConfig.getApi());
                Object obj = applicationContext.getBean(rocketProviderXmlConfig.getRef());
                rocketProviderDefinition.setClazz(obj.getClass());
                rocketProviderDefinition.setInstance(obj);
                rocketProviderDefinition.setProvider(provider);
                rocketProviderDefinition.setPort(port);
                rocketProviderDefinitionList.add(rocketProviderDefinition);
            }

        }
        return rocketProviderDefinitionList;
    }

    /**
     * 读取XML配置
     *
     * @return
     */
    private RocketProviderRootXmlConfig readXml() {
        Resource resource = new ClassPathResource(RESOURCE);
        BufferedReader br = null;
        RocketProviderRootXmlConfig rocketProviderRootXmlConfig = null;
        try {
            br = new BufferedReader(new InputStreamReader(resource.getInputStream(), "UTF-8"));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
            rocketProviderRootXmlConfig = XmlUtils.xmlToObject(RocketProviderRootXmlConfig.class, buffer.toString());
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
        return rocketProviderRootXmlConfig;
    }
}
