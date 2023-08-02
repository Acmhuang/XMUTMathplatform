package com.ruoyi.chat.config;

import com.ruoyi.chat.controller.WebSocket;
import com.ruoyi.chat.domain.CustomChatGpt;
import com.ruoyi.chat.service.UserChatRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {
    /**
     * 注入ServerEndpointExporter，
     * 这个bean会自动注册使用了@ServerEndpoint注解声明的Websocket endpoint
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 把UserChatRecordService添加到WebSocket中，以使用
     * @param userChatRecordService
     */
    @Autowired
    public void setUserChatRecordService(UserChatRecordService userChatRecordService) {
        WebSocket.userChatRecordService = userChatRecordService;
    }

    @Autowired
    public void setCustomChatGpt(CustomChatGpt customChatGpt){
        WebSocket.customChatGpt = customChatGpt;
    }
}
