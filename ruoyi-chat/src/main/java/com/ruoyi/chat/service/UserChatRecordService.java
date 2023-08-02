package com.ruoyi.chat.service;

import com.ruoyi.chat.domain.Message;
import com.ruoyi.chat.mapper.UserChatRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserChatRecordService {
    @Autowired
    private UserChatRecordMapper userChatRecordMapper;

    /**
     * 保存聊天记录
     * @param message
     */
    public void saveUserChatRecord(Message message) {
        userChatRecordMapper.saveChatRecord(message);
    }
}
