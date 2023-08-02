package com.ruoyi.chat.controller;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.chat.domain.ChatGPT;
import com.ruoyi.chat.domain.CustomChatGpt;
import com.ruoyi.chat.domain.Message;
import com.ruoyi.chat.service.UserChatRecordService;
import com.ruoyi.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.core5.http.HttpHost;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
@ServerEndpoint("/websocket/{userId}")
public class WebSocket {

    public static UserChatRecordService userChatRecordService;

    public static CustomChatGpt customChatGpt;

    HttpHost proxy = new HttpHost("127.0.0.1", 7890);
    DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
    CloseableHttpClient client = HttpClients.custom().setRoutePlanner(routePlanner).build();

    // 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static ConcurrentHashMap<String, WebSocket> webSocketConcurrentHashMap = new ConcurrentHashMap<>();

    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    // 接收userId
    private String userId = "";

    /**
     * 连接建立成功调用的方法
     *
     * @param session
     * @param userId
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") String userId) {

        this.session = session;
        this.userId = userId;
        if (webSocketConcurrentHashMap.containsKey(userId)) {
            webSocketConcurrentHashMap.remove(userId);
            //存入数据
            webSocketConcurrentHashMap.put(userId, this);
        } else {
            webSocketConcurrentHashMap.put(userId, this);
            //在线人数加一
            addOnlineCount();
        }
        //默认客户端，没有重名
        log.info("用户连接：{}，当前在线人数为：{}", userId, getOnlineCount());

        try {
            sendMessage("连接成功");
        } catch (IOException e) {
            log.info("用户：{}，网络异常！！！", userId);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if (webSocketConcurrentHashMap.containsKey(userId)) {
            webSocketConcurrentHashMap.remove(userId);
            //从set中删除
            subOnlineCount();
        }
        log.info("用户退出：{}，当前在线人数为：{}", userId, getOnlineCount());

    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param session
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        log.info("用户消息：{}，报文：{}", userId, message);

        // 将收到的消息保存到数据库
        Message receive = JSON.parseObject(message, Message.class);
        userChatRecordService.saveUserChatRecord(receive);

        // 调用方法创建回复消息对象（需要改成自己的key）
        //String content = customChatGpt.getAnswer(client, receive.getText());

        String content = "你好";
        log.info("回复消息：{}", content);
        Message send = Message.builder()
                .userId(userId)
                .text(content)
                .mine(false)
                .name(ChatGPT.name)
                .img(ChatGPT.img)
                .build();
        userChatRecordService.saveUserChatRecord(send);
        String s = JSON.toJSONString(send);
        sendMessage(s);
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.info("用户错误：{}，原因：{}", userId, error.getMessage());
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     *
     * @param message 推送消息
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
        log.info("服务器发送消息：{}", message);
    }

    /**
     * 数据库中更改后发送消息
     */
    public static void sendInfo(String message, @PathParam("userId") String userId) throws IOException {
        System.out.println(("发送消息到:" + userId + "，报文:" + message));
        if (StringUtils.isNotBlank(userId) && webSocketConcurrentHashMap.containsKey(userId)) {
            webSocketConcurrentHashMap.get(userId).sendMessage(message);
        } else {
            log.info("用户：{}，不在线", userId);
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocket.onlineCount--;
    }
}