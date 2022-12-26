package com.roc.rocket.provider.config.xml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author roc
 * @date 2022/11/8
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "rocket")
@XmlAccessorType(XmlAccessType.FIELD)
public class RocketProviderXmlConfig {

    @XmlAttribute(name = "interface")
    private String api;

    @XmlAttribute(name = "ref")
    private String ref;

    @XmlAttribute(name = "type")
    private String type;


}
