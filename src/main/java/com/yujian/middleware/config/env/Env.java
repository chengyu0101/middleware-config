
package com.yujian.middleware.config.env;

import java.util.List;

import com.yujian.middleware.config.util.IPAddress;
import com.yujian.middleware.config.util.IPRange;
import com.yujian.middleware.config.util.JdkRuntimeUtils;
import com.yujian.middleware.config.util.NetUtils;

/**
 * @author cy
 * @Date 2021/7/27 2:42 PM
 */
public enum Env {

     DEV("*", "192.168.0.0/16"),

     TEST("yujian-test-", "192.168.0.0/16"),

     STABLE( "mdc-stable-", "172.20.100.0/24"),

     PRE("jf-pre-", "172.21.43.0/24"),

     PROD("gdfc-pre-", "172.18.166.227,172.31.150.162,172.31.150.163,172.31.150.164");

    public final List<String>  hostPrefixList;
    public final List<IPRange> ipRangeList;
    static final String        ipAddress = NetUtils.getLocalHost();
    static final String        hostName  = JdkRuntimeUtils.getHostName();


    private Env(String defHostPrefixList, String defIpRanges) {
        
        String envTag = this.name().toLowerCase();

        EnvConfig conf = EnvConfig.load(envTag);

        if (conf == null) {
            conf = EnvConfig.build(envTag, defHostPrefixList, defIpRanges);
        }

        this.hostPrefixList = conf.hostPrefixList;
        this.ipRangeList = conf.ipRangeList;
    }
    
    public String getName() {
        return this.name().toLowerCase();
    }

    public static Env of(String envString) {
        for (Env env : Env.values()) {
            if (env.name().equalsIgnoreCase(envString)) {
                return env;
            }
        }
        return null;
    }

    private boolean validateIpAddress() {
        for (IPRange range : this.ipRangeList) {
            if (range.isIPAddressInRange(new IPAddress(ipAddress))) {
                return true;
            }
        }
        return false;
    }

    private boolean validateHostName() {
        for (String prefix : this.hostPrefixList) {
            if (EnvConfig.MATCH_ALL.equals(prefix) || hostName.toLowerCase().startsWith(prefix.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static Env parseAndValidate(String envString) {
        if (envString == null) {
            return null;
        }

        Env env = valueOf(envString);
        if (env == null) {
            env = valueOf(envString.toUpperCase());
        }
        if (env == null) {
            env = valueOf(envString.toLowerCase());
        }
        
        if (env != null) {
            if (!env.validateHostName()) {
                throw new IllegalStateException(String.format("环境标识%s 主机名校验失败，本地hostName:%s，要求主机名前缀%s", env.name(), hostName, env.hostPrefixList));
            }
            if (!env.validateIpAddress()) {
                throw new IllegalStateException(String.format("环境标识%s ip段校验失败，本机ip:%s，要求ip段%s", env.name(), ipAddress, env.ipRangeList));
            }
        }
        return env;
    }

    public String description() {
        StringBuilder sb = new StringBuilder();
        sb.append("Env{name=").append(this.name()).append(";hostPrefixList=");
        for (String prefix : this.hostPrefixList) {
            sb.append(prefix).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(";ipRanges=").append(this.ipRangeList).append("}");
        return sb.toString();
    }

    public static String descriptions() {
        StringBuilder sb = new StringBuilder("[");
        for (Env env : values()) {
            sb.append(env.description()).append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

    public static String names() {
        StringBuilder sb = new StringBuilder("[");
        for (Env env : values()) {
            sb.append(env.name()).append("|");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

}