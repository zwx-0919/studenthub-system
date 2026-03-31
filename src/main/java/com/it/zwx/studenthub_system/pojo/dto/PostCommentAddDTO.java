package com.it.zwx.studenthub_system.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostCommentAddDTO {
    @NotNull(message = "帖子ID不能为空")
    private Integer postId;
    @NotBlank(message = "评论内容不能为空")
    private String content;
}
