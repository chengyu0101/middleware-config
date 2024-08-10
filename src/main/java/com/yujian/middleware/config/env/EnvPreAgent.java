
package com.yujian.middleware.config.env;

/**
 * @author cy
 * @Date 2021/7/27 2:42 PM
 */
public class EnvPreAgent {

    public static void premain(String agentArgs) {
        MiddlewareEnv.validateInit();
    }
}
