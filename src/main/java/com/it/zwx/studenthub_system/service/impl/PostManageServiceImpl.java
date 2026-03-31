package com.it.zwx.studenthub_system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.exception.BusinessException;
import com.it.zwx.studenthub_system.pojo.dto.PostAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.PostPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Post;
import com.it.zwx.studenthub_system.service.PostManageService;
import com.it.zwx.studenthub_system.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostManageServiceImpl implements PostManageService {
    private final PostService postService;

    @Override
    public Page<Post> page(PostPageDTO dto) {
        return postService.adminPage(dto);
    }

    @Override
    public Post add(PostAddDTO dto) {
        return postService.add(dto);
    }

    @Override
    public void update(Integer id, PostAddDTO dto) {
        Post post = postService.getById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        post.setCategory(dto.getCategory());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setImageUrl(dto.getImageUrl());
        post.setUpdateTime(LocalDateTime.now());
        postService.updateById(post);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        postService.updateStatus(id, status);
    }

    @Override
    public void delete(List<Integer> ids) {
        postService.deleteByIds(ids);
    }
}
