package com.yujian.middleware.config.env;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.yujian.middleware.config.util.IPRange;

/**
 * @author cy
 * @Date 2021/7/27 2:42 PM
 */

class EnvConfig {
    final static String             ENV_CONFIG_FILE_PATH;
    private final static Properties CONFIG;
    private static final String     SEPARATOR_0 = ";";
    private static final String     SEPARATOR_1 = ",";
    static final String             MATCH_ALL   = "*";

    static {
        /* 
         * dev=*;192.168.0.0/16
         * test=mdc-test-;172.20.100.0/24
         * stable=mdc-stable-;172.20.100.0/24
         * pre=jf-pre-;172.21.43.0/24
         * prd=jf-prd-;172.21.0.0/16,172.20.1.0/24,172.20.3.0/24,172.20.5.0/24,172.21.30.0/24,172.20.253.0/24
         */
        File file;
        String conf = System.getProperty("middleware.env.file");
        if (conf != null && conf.length() > 0 && new File(conf).exists() && new File(conf).isFile()) {
            file = new File(conf);
        } else {
            File f = new File(System.getenv("user.home"), ".environment.config");
            if (f.exists() && f.isFile()) {
                conf = f.getAbsolutePath();
            } else {
                conf = "/root/public/environment";
            }
        }

        file = new File(conf);

        if (file.exists() && file.isFile()) {
            ENV_CONFIG_FILE_PATH = file.getAbsolutePath();

            System.out.println("load environment config from file://" + ENV_CONFIG_FILE_PATH);
        } else {
            System.out.println("environment.config[" + conf + "]不存在");
            ENV_CONFIG_FILE_PATH = null;
        }

        CONFIG = new Properties();
        if (ENV_CONFIG_FILE_PATH != null) {
            try {
                CONFIG.load(new FileInputStream(ENV_CONFIG_FILE_PATH));
                System.out.println(String.format("加载文件%s成功.", ENV_CONFIG_FILE_PATH));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) {
                    System.out.println(String.format("未添加文件%s将使用默认配置", ENV_CONFIG_FILE_PATH));
                } else {
                    System.out.println(String.format("加载文件%s失败，将使用默认配置", ENV_CONFIG_FILE_PATH));
                }
            }
        }
    }

    final String        name;
    final List<String>  hostPrefixList;
    final List<IPRange> ipRangeList;

    public EnvConfig(String name, List<String> hostPrefixList, List<IPRange> ipRangeList) {
        this.name = name;
        this.hostPrefixList = hostPrefixList;
        this.ipRangeList = ipRangeList;
    }

    static EnvConfig build(String name, String hostPrefixList, String ipRanges) {
        List<String> hosts = new ArrayList<String>();
        if (hostPrefixList != null) {
            hosts = Arrays.asList(hostPrefixList.split(SEPARATOR_1));
        }

        List<IPRange> ipRang = new ArrayList<IPRange>();
        if (ipRanges != null) {
            String[] ipr = ipRanges.split(SEPARATOR_1);
            for (String r : ipr) {
                ipRang.add(new IPRange(r));
            }
        }

        return new EnvConfig(name, hosts, ipRang);
    }

    static EnvConfig load(String ename) {
        String c = EnvConfig.CONFIG.getProperty(ename);
        if (c != null) {
            String[] configSegments = c.split(SEPARATOR_0);
            if (configSegments.length != 2) {
                System.out.println(String.format("配置文件%s中配置项%s=%s格式不符合规范", ENV_CONFIG_FILE_PATH, ename, c));
            } else {
                List<String> hosts = new ArrayList<String>();
                if (configSegments[0] != null) {
                    hosts = Arrays.asList(configSegments[0].split(SEPARATOR_1));
                }

                List<IPRange> ipRang = new ArrayList<IPRange>();
                if (configSegments[1] != null) {
                    String[] ipr = configSegments[0].split(SEPARATOR_1);
                    for (String r : ipr) {
                        ipRang.add(new IPRange(r));
                    }
                }
                EnvConfig ld = new EnvConfig(ename, hosts, ipRang);

                System.out.println(String.format("配置文件%s中包含配置项%s=%s,将使用该配置替代默认配置", ENV_CONFIG_FILE_PATH, ename, c));

                return ld;
            }
        }

        return null;

    }
}
