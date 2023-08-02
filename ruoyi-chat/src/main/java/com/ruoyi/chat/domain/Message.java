package com.ruoyi.chat.domain;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String userId;
    //内容
    private String text;
    //是否本人发送
    private boolean mine;
    //名字
    private String name;
    //头像
    private String img;
}
