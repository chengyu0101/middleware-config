
package com.yujian.middleware.config.trace;

/**
 * @author cy
 * @Date 2021/7/27 2:42 PM
 */
public class MiddlewareTraceHolder {
    private static ThreadLocal<MiddlewareTrace> threadLocal = new ThreadLocal<MiddlewareTrace>();

    public static MiddlewareTrace get() {
        return threadLocal.get();
    }

    public static void set(MiddlewareTrace trace) {
        threadLocal.set(trace);
    }

    public static void clear() {
        threadLocal.remove();
    }
}
