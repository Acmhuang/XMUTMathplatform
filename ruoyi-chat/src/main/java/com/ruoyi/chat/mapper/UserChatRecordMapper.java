package com.ruoyi.chat.mapper;

import com.ruoyi.chat.domain.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserChatRecordMapper {
    /**
     * 保存聊天记录
     * @param message
     */
    @Insert("insert into sys_user_chat(user_id,text,mine) values(#{userId},#{text},#{mine})")
    void saveChatRecord(Message message);

}
