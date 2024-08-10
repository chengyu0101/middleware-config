
package com.yujian.middleware.config.env;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.yujian.middleware.config.util.NetUtils;

/**
 * @author cy
 * @Date 2021/7/27 2:42 PM
 */
public class MiddlewareEnv {

    static final String             LOCAL_ENV_FILE_PATH               = "/root/public/environment";
    static final String             DEVELOPER_ADDRESS                 = "192.168.";
    static final String             MIDDLEWARE_SYSTEM_PROPERTY_PREFIX = "middleware.";
    static final String             DEVELOPER_MARK                    = "developer";
    static final String             ENV_KEY                           = "env";
    private static Properties       localProperties                   = new Properties();
    private static volatile boolean initialized;
    private static Object           lock                              = new Object();
    private static Env              currentEnv;

    private static void init() {
        String evnTag = System.getProperty(MIDDLEWARE_SYSTEM_PROPERTY_PREFIX + ENV_KEY);
        if (evnTag != null) {
            currentEnv = Env.of(evnTag);
            if (currentEnv == null) {
                throw new IllegalStateException(String.format("设置的jvm参数%s的值%s不符合规范：%s", MIDDLEWARE_SYSTEM_PROPERTY_PREFIX + ENV_KEY, evnTag, Env.names()));
            }
        } else {
            try {
                localProperties.load(new FileInputStream(LOCAL_ENV_FILE_PATH));
            } catch (IOException e) {
                if (!isDeveloperAddress()) {
                    throw new IllegalStateException(String.format("基础机器配置文件%s不存在、或无读取权限、或文件异常，请联系运维解决", LOCAL_ENV_FILE_PATH), e);
                }
            }
            evnTag = localProperties.getProperty(ENV_KEY);
            if (evnTag == null) {
                if (isDeveloperAddress()) {
                    currentEnv = Env.DEV;
                } else {
                    throw new IllegalStateException(String.format("基础机器配置文件%s中，环境标识%s不存在", LOCAL_ENV_FILE_PATH, ENV_KEY));
                }
            } else if ((currentEnv = Env.parseAndValidate(evnTag)) == null) {
                throw new IllegalStateException(String.format("基础机器配置文件%s中，环境标识%s不符合规范：%s", LOCAL_ENV_FILE_PATH, ENV_KEY, Env.names()));
            }
        }
        System.out.println("MiddlewareEnv识别到的环境标识为:" + currentEnv.name());
        initialized = true;
    }

    private static boolean isDeveloperAddress() {
        return "true".equalsIgnoreCase(System.getProperty(MIDDLEWARE_SYSTEM_PROPERTY_PREFIX + DEVELOPER_MARK)) || NetUtils.getLocalHost().startsWith(DEVELOPER_ADDRESS);
    }

    public static void validateInit() {
        if (currentEnv == null) {
            if (!initialized) {
                synchronized (lock) {
                    if (!initialized) {
                        init();
                    }
                }
            } else {
                throw new IllegalStateException(String.format("获取环境标识信息失败，请联系运维检查基础配置文件%s是否与环境信息设置匹配：%s", LOCAL_ENV_FILE_PATH, Env.descriptions()));
            }
        }
    }

    public static boolean isDev() {
        validateInit();
        return currentEnv == Env.DEV;
    }

    public static boolean isTest() {
        validateInit();
        return currentEnv == Env.TEST;
    }

    public static boolean isStable() {
        validateInit();
        return currentEnv == Env.STABLE;
    }

    public static boolean isPre() {
        validateInit();
        return currentEnv == Env.PRE;
    }

    public static boolean isPrd() {
        validateInit();
        return currentEnv == Env.PROD;
    }

    public static String getEnvName() {
        validateInit();
        return currentEnv.name();
    }

    public static String getAttribute(String key) {
        validateInit();
        String value = System.getProperty(MIDDLEWARE_SYSTEM_PROPERTY_PREFIX + key);
        if (value != null) {
            return value;
        }
        return localProperties.getProperty(key);
    }

}
