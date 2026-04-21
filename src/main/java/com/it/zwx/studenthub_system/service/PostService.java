package com.it.zwx.studenthub_system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.it.zwx.studenthub_system.pojo.dto.PostAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.PostCommentAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.PostPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Post;
import com.it.zwx.studenthub_system.pojo.entity.PostComment;
import com.it.zwx.studenthub_system.pojo.vo.PostDetailVO;
import com.it.zwx.studenthub_system.pojo.vo.PostLikeResultVO;

import java.util.List;

public interface PostService extends IService<Post> {
    Post add(PostAddDTO dto);

    Page<Post> pagePublished(PostPageDTO dto);

    Page<Post> adminPage(PostPageDTO dto);

    void updateStatus(Integer id, Integer status);

    void deleteByIds(List<Integer> ids);

    PostDetailVO detail(Integer id);

    PostComment addComment(PostCommentAddDTO dto);

    PostLikeResultVO toggleLike(Integer postId);

    void deleteMine(Integer postId);

    void deleteComment(Integer commentId);
}

