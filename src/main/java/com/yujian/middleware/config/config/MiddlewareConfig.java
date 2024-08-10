
package com.yujian.middleware.config.config;

import com.yujian.middleware.config.Constants;

/**
 * @author cy
 * @Date 2021/7/27 2:42 PM
 */
public class MiddlewareConfig {

    private static String applicationCode;
    private static volatile boolean initialized;
    private static Object init_lock = new Object();

    static {
        String code = System.getProperty("env.app.code");

        if (code != null && code.trim().length() > 0) {
            applicationCode = code.trim().toUpperCase();
            initialized = true;
        }
    }

    public static String getApplicationCode() {
        String codeInSystemProperties = System.getProperty(Constants.MIDDLEWARE_CONFIG_APPLICATION_CODE);
        if (codeInSystemProperties != null && codeInSystemProperties.trim().length() > 0) {
            return codeInSystemProperties.trim().toUpperCase();
        }
        if (applicationCode != null) {
            return applicationCode;
        }
        throw new IllegalStateException("middlewareConfigApplicationCode is not set.");
    }

    static void setApplicationCode(String applicationCode) {
        if (applicationCode == null || applicationCode.trim().length() == 0) {
            throw new IllegalStateException("middlewareConfigApplicationCode set parameter is empty.");
        }

        if (!initialized) {
            synchronized (init_lock) {
                if (!initialized) {
                    MiddlewareConfig.applicationCode = applicationCode.trim().toUpperCase();
                } else {
                    System.out.println(String.format("applicationCode[%s]已初始化完成", MiddlewareConfig.applicationCode));
                }
            }
        } else {
            System.out.println(String.format("applicationCode[%s]已初始化完成", MiddlewareConfig.applicationCode));
        }

    }

}
