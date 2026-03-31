package com.it.zwx.studenthub_system.pojo.vo;

import com.it.zwx.studenthub_system.pojo.entity.Post;
import com.it.zwx.studenthub_system.pojo.entity.PostComment;
import lombok.Data;

import java.util.List;

@Data
public class PostDetailVO {
    private Post post;
    private List<PostComment> comments;
}
