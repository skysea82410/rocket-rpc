package com.roc.rocket.consumer.api.config;

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
public class RocketConsumerXmlConfig {

    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "interface")
    private String api;

    @XmlAttribute(name = "type")
    private String type;

}
