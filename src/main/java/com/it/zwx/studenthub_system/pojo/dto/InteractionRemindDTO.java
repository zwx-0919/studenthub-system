package com.it.zwx.studenthub_system.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InteractionRemindDTO {
    @NotNull(message = "接收人不能为空")
    private Integer receiverId;

    @NotBlank(message = "互动人不能为空")
    private String actorName;

    @NotBlank(message = "互动动作不能为空")
    private String action;

    @NotBlank(message = "帖子标题不能为空")
    private String postTitle;

    private Integer bizId;
}

