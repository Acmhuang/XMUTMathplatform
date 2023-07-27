package com.ruoyi.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Message {
    private String time;

    private Content text;

    private boolean mine;

    private String name;

    private String img;
}
