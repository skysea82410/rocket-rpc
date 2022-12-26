package com.roc.rocket.consumer.api.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author roc
 * @date 2022/11/8
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "beans")
@XmlAccessorType(XmlAccessType.FIELD)
public class RocketConsumerRootXmlConfig {

    @XmlElement(name = "rocket")
    private List<RocketConsumerXmlConfig> rocketConsumerXmlConfigList;

}
