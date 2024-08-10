
package com.yujian.middleware.config.trace;

import lombok.Builder;
import lombok.Getter;

/**
 * @author cy
 * @Date 2021/7/27 2:42 PM
 */
@Builder
@Getter
public class MiddlewareTrace {

    private String traceId;

    private String userId;
}
