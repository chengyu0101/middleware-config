
package com.yujian.middleware.config.config;

import com.yujian.middleware.config.Constants;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * @author cy
 * @Date 2021/7/27 2:42 PM
 */
public class MiddlewareConfigListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        MiddlewareConfigSetter.setApplicationCode(sce.getServletContext().getInitParameter(Constants.MIDDLEWARE_CONFIG_APPLICATION_CODE));
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
