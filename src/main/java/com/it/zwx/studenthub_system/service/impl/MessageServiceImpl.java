package com.it.zwx.studenthub_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.zwx.studenthub_system.exception.BusinessException;
import com.it.zwx.studenthub_system.mapper.MessageMapper;
import com.it.zwx.studenthub_system.pojo.dto.InteractionRemindDTO;
import com.it.zwx.studenthub_system.pojo.dto.MessageSendDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.MessagePageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Message;
import com.it.zwx.studenthub_system.pojo.entity.User;
import com.it.zwx.studenthub_system.service.MessageService;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.ThreadLocalUtil;
import com.it.zwx.studenthub_system.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final UserService userService;
    private final WebSocketService webSocketService;

    @Override
    public Message send(MessageSendDTO dto) {
        User user = currentUser();
        Message message = new Message();
        message.setSenderId(user.getId());
        message.setReceiverId(dto.getReceiverId());
        message.setMessageType(dto.getMessageType() == null ? 2 : dto.getMessageType());
        message.setContent(dto.getContent());
        message.setIsRead(0);
        message.setSendTime(LocalDateTime.now());
        // 私信提醒的“详情”需要跳转到聊天对象：这里用 bizId 承载 senderId（给接收方打开 /chat/{senderId}）
        if (message.getMessageType() != null && message.getMessageType() == 2) {
            message.setBizId(user.getId());
        } else {
            message.setBizId(dto.getBizId());
        }
        baseMapper.insert(message);

        webSocketService.pushReminder(dto.getReceiverId(), message.getMessageType(), message.getContent(), message.getBizId());
        return message;
    }

    @Override
    public Page<Message> myPage(MessagePageDTO dto) {
        User user = currentUser();
        Page<Message> page = new Page<>(dto.getCurrent(), dto.getSize());
        QueryWrapper<Message> qw = new QueryWrapper<>();
        qw.eq("receiver_id", user.getId())
                .eq(dto.getMessageType() != null, "message_type", dto.getMessageType())
                .eq(dto.getIsRead() != null, "is_read", dto.getIsRead())
                .orderByDesc("send_time");
        baseMapper.selectPage(page, qw);
        page.getRecords().forEach(item -> {
            if (item.getSenderId() == null || item.getSenderId() == 0) {
                item.setSenderName("系统");
            } else {
                User sender = userService.getById(item.getSenderId());
                item.setSenderName(sender == null ? "用户" : sender.getUserName());
            }
        });
        return page;
    }

    @Override
    public void read(Integer id) {
        User user = currentUser();
        Message message = baseMapper.selectById(id);
        if (message == null || !message.getReceiverId().equals(user.getId())) {
            throw new BusinessException("消息不存在或无权限");
        }
        message.setIsRead(1);
        baseMapper.updateById(message);
    }

    @Override
    public long unreadCount() {
        User user = currentUser();
        QueryWrapper<Message> qw = new QueryWrapper<>();
        qw.eq("receiver_id", user.getId()).eq("is_read", 0);
        return baseMapper.selectCount(qw);
    }

    @Override
    public void pushSystem(Integer receiverId, String content, Integer bizId) {
        Message message = new Message();
        message.setSenderId(0);
        message.setReceiverId(receiverId);
        message.setMessageType(1);
        message.setContent(content);
        message.setIsRead(0);
        message.setSendTime(LocalDateTime.now());
        message.setBizId(bizId);
        baseMapper.insert(message);
        webSocketService.pushReminder(receiverId, message.getMessageType(), content, bizId);
    }

    @Override
    public void pushInteraction(InteractionRemindDTO dto) {
        String text = String.format("%s%s了你的帖子《%s》", dto.getActorName(), dto.getAction(), dto.getPostTitle());
        Message message = new Message();
        message.setSenderId(0);
        message.setReceiverId(dto.getReceiverId());
        message.setMessageType(3);
        message.setContent(text);
        message.setIsRead(0);
        message.setSendTime(LocalDateTime.now());
        message.setBizId(dto.getBizId());
        baseMapper.insert(message);
        webSocketService.pushReminder(dto.getReceiverId(), message.getMessageType(), text, message.getBizId());
    }

    private User currentUser() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null || claims.get("id") == null) {
            throw new BusinessException("用户未登录");
        }
        Integer uid = claims.get("id") instanceof Integer ? (Integer) claims.get("id") : ((Number) claims.get("id")).intValue();
        User user = userService.getById(uid);
        if (user == null) throw new BusinessException("用户不存在");
        return user;
    }
}

