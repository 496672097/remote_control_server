package com.remotecontrol.star.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * @author ：复目
 * @date ：Created in 2021/11/9 17:52
 */

@Configuration
public class WebSocketConfig implements WebMvcConfigurer {
    // 20MB
    private static final int MAX_MESSAGE_SIZE = 100 * 1024 * 1024;
    private static final long MAX_IDLE = 60 * 60 * 1000;

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Bean
    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(MAX_MESSAGE_SIZE);
        container.setMaxBinaryMessageBufferSize(MAX_MESSAGE_SIZE);
        container.setMaxSessionIdleTimeout(MAX_IDLE);
        return container;
    }
}