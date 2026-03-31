package com.it.zwx.studenthub_system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.it.zwx.studenthub_system.pojo.dto.InteractionRemindDTO;
import com.it.zwx.studenthub_system.pojo.dto.MessageSendDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.MessagePageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Message;

public interface MessageService extends IService<Message> {
    Message send(MessageSendDTO dto);
    Page<Message> myPage(MessagePageDTO dto);
    void read(Integer id);
    long unreadCount();
    void pushSystem(Integer receiverId, String content, Integer bizId);
    void pushInteraction(InteractionRemindDTO dto);
}

