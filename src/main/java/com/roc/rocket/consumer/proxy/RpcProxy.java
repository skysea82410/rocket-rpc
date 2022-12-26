package com.roc.rocket.consumer.proxy;

import com.roc.rocket.consumer.RpcExecutor;
import com.roc.rocket.protocol.RocketProtocol;
import com.roc.rocket.protocol.RocketProtocolBody;
import com.roc.rocket.utils.ApplicationContextUtils;
import com.roc.rocket.utils.UuidUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author roc
 * @date 2022/11/16
 */
@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
@Scope("prototype")
public class RpcProxy implements InvocationHandler {

    private Class<?> clazz;

    private String api;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (proxy.getClass().equals(method.getDeclaringClass())) {
            return method.invoke(proxy, args);
        } else {
            return rpcInvoke(proxy, method, args);
        }
    }

    private Object rpcInvoke(Object proxy, Method method, Object[] args) throws Exception {
        //生成协议体
        RocketProtocolBody rocketProtocolBody = new RocketProtocolBody();
        rocketProtocolBody.setFullClassName(this.clazz.getName());
        rocketProtocolBody.setMethodName(method.getName());
        rocketProtocolBody.setMethodValues(args);
        RocketProtocol rocketProtocol = new RocketProtocol(UuidUtils.createId(), rocketProtocolBody);
        RpcExecutor rpcExecutor = ApplicationContextUtils.getBean(RpcExecutor.class);
        //执行远程调用方法
        return rpcExecutor.call(rocketProtocol, api);
    }
}
