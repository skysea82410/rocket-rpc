package com.roc.rocket.provider.config.xml;

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
public class RocketProviderRootXmlConfig {

    @XmlElement(name = "rocket")
    private List<RocketProviderXmlConfig> rocketProviderXmlConfigList;

}
