
package com.yujian.middleware.config.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * @author cy
 * @Date 2021/7/27 2:42 PM
 */
public class NetUtils {

    public static final String LOCALHOST = "127.0.0.1";

    public static final String ANYHOST = "0.0.0.0";

    public static final String DEV_PREFER_IP_PREFIX = "192.168.";

    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    private static volatile String localAddressString;

    private static volatile InetAddress LOCAL_ADDRESS;

    public static String getLocalHost() {
        if (localAddressString!=null) {
            return localAddressString;
        }
        InetAddress address = getLocalAddress();
        if (address == null) {
            localAddressString = LOCALHOST;
        } else {
            localAddressString = address.getHostAddress();
        }
        return localAddressString;
    }

    /**
     * 遍历本地网卡，返回第一个合理的IP。
     *
     * 如果合理ip中包含192.168开头ip，优先返回该ip，防止开发人员开vpn情况
     *
     * @return 本地网卡IP
     */
    public static InetAddress getLocalAddress() {
        if (LOCAL_ADDRESS != null) {
            return LOCAL_ADDRESS;
        }
        InetAddress localAddress = getLocalAddress0();
        LOCAL_ADDRESS = localAddress;
        return localAddress;
    }

    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable e) {
            System.out.println("Failed to retriving ip address, " + e.getMessage());
            e.printStackTrace();
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                InetAddress firstValid = null;
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        if (addresses != null) {
                            while (addresses.hasMoreElements()) {
                                try {
                                    InetAddress address = addresses.nextElement();
                                    if (isValidAddress(address)) {
                                        if (firstValid == null) {
                                            firstValid = address;
                                        }
                                        if (address.getHostAddress().startsWith(DEV_PREFER_IP_PREFIX)) {
                                            return address;
                                        }
                                    }
                                } catch (Throwable e) {
                                    System.out.println("Failed to retriving ip address, " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (Throwable e) {
                        System.out.println("Failed to retriving ip address, " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                if (firstValid != null) {
                    return firstValid;
                }
            }
        } catch (Throwable e) {
            System.out.println("Failed to retriving ip address, " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Could not get local host ip address, will use 127.0.0.1 instead.");
        return localAddress;
    }


    private static boolean isValidAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }
        String name = address.getHostAddress();
        return (name != null && !ANYHOST.equals(name) && !LOCALHOST.equals(name) && IP_PATTERN.matcher(name).matches());
    }

}
