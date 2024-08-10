
package com.yujian.middleware.config.util;

import java.lang.management.ManagementFactory;

/**
 * @author cy
 * @Date 2021/7/27 2:42 PM
 */
public class JdkRuntimeUtils {

    private static volatile String processId;
    private static volatile String hostName;

    public static final String getProcessId() {
        if (processId == null) {
            try {
                String jvmName = ManagementFactory.getRuntimeMXBean().getName();
                processId = jvmName.split("@")[0];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return processId;
    }

    public static final String getHostName() {
        if (hostName == null) {
            try {
                String jvmName = ManagementFactory.getRuntimeMXBean().getName();
                hostName = jvmName.split("@")[1];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hostName;
    }

}
