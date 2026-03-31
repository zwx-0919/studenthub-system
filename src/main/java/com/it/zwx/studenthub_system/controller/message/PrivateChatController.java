package com.it.zwx.studenthub_system.controller.message;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.it.zwx.studenthub_system.exception.BusinessException;
import com.it.zwx.studenthub_system.mapper.PrivateChatMapper;
import com.it.zwx.studenthub_system.mapper.MessageMapper;
import com.it.zwx.studenthub_system.pojo.dto.PrivateChatSendDTO;
import com.it.zwx.studenthub_system.pojo.entity.Message;
import com.it.zwx.studenthub_system.pojo.entity.PrivateChat;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.pojo.entity.User;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.ThreadLocalUtil;
import com.it.zwx.studenthub_system.websocket.WebSocketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/private-chat")
@Tag(name = "私信聊天")
@RequiredArgsConstructor
public class PrivateChatController {

    private final PrivateChatMapper privateChatMapper;
    private final MessageMapper messageMapper;
    private final UserService userService;
    private final WebSocketService webSocketService;

    @GetMapping("/conversation/{targetUserId}")
    @Operation(summary = "查看与目标用户的聊天记录")
    public Result<List<PrivateChat>> conversation(@PathVariable Integer targetUserId) {
        Integer uid = currentUid();
        QueryWrapper<PrivateChat> qw = new QueryWrapper<>();
        qw.and(w -> w.eq("sender_id", uid).eq("receiver_id", targetUserId)
                        .or()
                        .eq("sender_id", targetUserId).eq("receiver_id", uid))
                .orderByDesc("send_time");
        List<PrivateChat> list = privateChatMapper.selectList(qw);

        QueryWrapper<PrivateChat> readQw = new QueryWrapper<>();
        readQw.eq("sender_id", targetUserId).eq("receiver_id", uid).eq("is_read", 0);
        List<PrivateChat> unread = privateChatMapper.selectList(readQw);
        for (PrivateChat item : unread) {
            item.setIsRead(1);
            privateChatMapper.updateById(item);
        }
        return Result.success(list);
    }

    @PostMapping("/send")
    @Operation(summary = "发送私信")
    public Result<PrivateChat> send(@RequestBody PrivateChatSendDTO dto) {
        Integer uid = currentUid();
        User target = userService.getById(dto.getReceiverId());
        if (target == null) throw new BusinessException("接收用户不存在");
        PrivateChat chat = new PrivateChat();
        chat.setSenderId(uid);
        chat.setReceiverId(dto.getReceiverId());
        chat.setContent(dto.getContent());
        chat.setSendTime(LocalDateTime.now());
        chat.setIsRead(0);
        privateChatMapper.insert(chat);

        // 同步写入 message 表，确保“铃铛消息中心”有记录
        Message msg = new Message();
        msg.setSenderId(uid);
        msg.setReceiverId(dto.getReceiverId());
        msg.setMessageType(2);
        msg.setContent(dto.getContent());
        msg.setIsRead(0);
        msg.setSendTime(LocalDateTime.now());
        // bizId 用发送者ID，便于接收方弹窗“查看详情”直接进入聊天框 /chat/{senderId}
        msg.setBizId(uid);
        messageMapper.insert(msg);

        webSocketService.pushReminder(dto.getReceiverId(), 2, msg.getContent(), msg.getBizId());
        return Result.success(chat);
    }

    private Integer currentUid() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null || claims.get("id") == null) throw new BusinessException("用户未登录");
        return claims.get("id") instanceof Integer ? (Integer) claims.get("id") : ((Number) claims.get("id")).intValue();
    }
}
