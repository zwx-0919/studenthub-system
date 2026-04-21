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
    /** 回复某条评论时传入父评论 ID */
    private Integer parentId;
}
