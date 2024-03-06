package com.ljx.utils;

import com.ljx.Exceptions.NetworkException;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @Author LiuJixing
 * @Date 4/3/2024
 */
@Slf4j
public class NetUtils {
    public static String getLocalIp() {
        try{
            Enumeration<java.net.NetworkInterface> allNetInterfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface iface = allNetInterfaces.nextElement();
                if (iface.isLoopback() || iface.isVirtual() || !iface.isUp() || iface.isPointToPoint()) {
                    continue;
                }
                Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if(addr.isLoopbackAddress() || addr instanceof Inet6Address) {
                        continue;
                    }
                    String ip = addr.getHostAddress();
                    if(log.isDebugEnabled()){
                        log.debug("本机的局域网ip={}", ip);
                    }
                    return ip;
                }
            }
            throw new NetworkException();
        } catch (SocketException e) {
            log.error("获取本机ip地址时发生异常：{}", e.getMessage());
            throw new NetworkException();
        }
    }
}
