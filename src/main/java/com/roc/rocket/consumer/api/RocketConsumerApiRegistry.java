package com.roc.rocket.consumer.api;

import com.roc.common.utils.CollectionUtils;
import com.roc.rocket.consumer.api.reader.DefaultRocketConsumerResourceReader;
import com.roc.rocket.consumer.api.reader.RocketConsumerResouceReader;
import com.roc.rocket.consumer.def.RocketConsumerDefinition;
import com.roc.rocket.consumer.proxy.RocketRpcProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author roc
 * @date 2022/11/10
 */
@Configuration
public class RocketConsumerApiRegistry implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        RocketConsumerResouceReader rocketConsumerResouceReader = new RocketConsumerResouceReader(new DefaultRocketConsumerResourceReader());
        //读取consumer配置，这里也采用XML配置文件方式
        List<RocketConsumerDefinition> rocketConsumerDefinitionList = rocketConsumerResouceReader.read();
        if (CollectionUtils.isNotEmpty(rocketConsumerDefinitionList)) {
            for (RocketConsumerDefinition rocketConsumerDefinition : rocketConsumerDefinitionList) {
                try {
                    Class<?> cls = Class.forName(rocketConsumerDefinition.getApi());
                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(cls);
                    //生成注册bean需要的bean definition
                    GenericBeanDefinition definition = (GenericBeanDefinition) beanDefinitionBuilder.getRawBeanDefinition();
                    definition.getPropertyValues().add("clazz", cls);
                    definition.getPropertyValues().add("api", rocketConsumerDefinition.getApi());
                    //生成代理类的关键
                    definition.setBeanClass(RocketRpcProxyFactory.class);
                    definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
                    // 注册bean名,一般为类名首字母小写
                    beanDefinitionRegistry.registerBeanDefinition(rocketConsumerDefinition.getId(), definition);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

}
