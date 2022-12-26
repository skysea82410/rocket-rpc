package com.roc.rocket.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @author roc
 * @date 2022/11/30
 */
@Slf4j
public class NetUtils {

    /**
     * 获取主机 IP
     */
    public static String getLocalIpAddr() {
        String ip = null;
        try {
            // 遍历所有的网络接口
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {
                        // 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            ip = inetAddr.getHostAddress();
                        }
                    }
                }
            }
            if (ip != null) {
                return ip;
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("未获取到主机IP");
            }
            ip = jdkSuppliedAddress.getHostAddress();
            return ip;
        } catch (Exception e) {
            log.error("获取主机IP异常", e);
        }
        return null;
    }
}
