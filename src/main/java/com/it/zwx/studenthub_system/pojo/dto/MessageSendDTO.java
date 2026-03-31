package com.it.zwx.studenthub_system.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageSendDTO {
    @NotNull(message = "接收人不能为空")
    private Integer receiverId;
    @NotBlank(message = "消息内容不能为空")
    private String content;
    private Integer messageType; // 2私信 3互动提醒
    private Integer bizId;
}

