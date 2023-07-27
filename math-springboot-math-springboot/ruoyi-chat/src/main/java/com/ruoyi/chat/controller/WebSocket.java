package com.ruoyi.chat.controller;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.chat.domain.Content;
import com.ruoyi.chat.domain.Message;
import com.ruoyi.chat.service.UserChatRecordService;
import com.ruoyi.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
@ServerEndpoint("/websocket/{userId}")
public class WebSocket {

    public static UserChatRecordService userChatRecordService;

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
        //可以群发消息
        // 消息保存到数据库、redis
        Message msg = JSON.parseObject(message, Message.class);

        Message ans = new Message();

        // 获取当前的LocalDateTime对象
        LocalDateTime now = LocalDateTime.now();
        // 定义要使用的日期格式
        String pattern = "yyyy/MM/dd HH:mm:ss";
        // 创建一个DateTimeFormatter对象，并指定日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        // 将LocalDateTime对象格式化为字符串
        String formattedDateTime = now.format(formatter);
        ans.setTime(formattedDateTime);

        Content content = new Content();
        content.setText("你好！如有任何问题或需要帮助，请随时向我询问。我会尽力为您提供准确和有用的答案。");

        ans.setText(content);
        ans.setMine(false);
        ans.setName("ChatGPT");
        ans.setImg("https://ts1.cn.mm.bing.net/th/id/R-C.44f7b44e144c3e62a7bf26d3ed9dbc25?rik=4mNHzi4yH%2fL%2bGQ&riu=http%3a%2f%2fwww.kuaipng.com%2fUploads%2fpic%2fw%2f2023%2f02-09%2f135403%2fwater_135403_698_698_.png&ehk=D%2bT8GsjcmjFS9uaNJ2JvSOP8R8kbzcmFHLdm8GV69lU%3d&risl=&pid=ImgRaw&r=0");

        String s = JSON.toJSONString(ans);
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