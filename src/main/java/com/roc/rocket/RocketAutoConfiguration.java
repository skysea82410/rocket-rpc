package com.roc.rocket;

import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author roc
 * @date 2022/11/22
 */
@Configuration
@ConditionalOnResource(resources = {"rocket-producer.xml", "rocket-consumer.xml"})
@ComponentScan(basePackages = {"com.roc.rocket"})
public class RocketAutoConfiguration {
    
}
