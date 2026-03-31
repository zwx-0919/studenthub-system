package com.it.zwx.studenthub_system.controller.message;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.pojo.dto.InteractionRemindDTO;
import com.it.zwx.studenthub_system.pojo.dto.MessageSendDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.MessagePageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Message;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
@Tag(name = "消息与私信")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    @Operation(summary = "发送私信/互动提醒")
    public Result<Message> send(@RequestBody @Validated MessageSendDTO dto) {
        return Result.success(messageService.send(dto));
    }

    @PostMapping("/interaction")
    @Operation(summary = "发送互动提醒（点赞/评论帖子）")
    public Result interaction(@RequestBody @Validated InteractionRemindDTO dto) {
        messageService.pushInteraction(dto);
        return Result.success("互动提醒已推送");
    }

    @GetMapping("/page")
    @Operation(summary = "消息列表查询（按类型/已读）")
    public Result<Page<Message>> page(@ModelAttribute MessagePageDTO dto) {
        return Result.success(messageService.myPage(dto));
    }

    @PatchMapping("/read/{id}")
    @Operation(summary = "标记消息已读")
    public Result read(@PathVariable Integer id) {
        messageService.read(id);
        return Result.success("已读");
    }

    @GetMapping("/unread-count")
    @Operation(summary = "未读消息数")
    public Result<Map<String, Object>> unreadCount() {
        Map<String, Object> map = new HashMap<>();
        map.put("unreadCount", messageService.unreadCount());
        return Result.success(map);
    }
}

