package com.it.zwx.studenthub_system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.pojo.dto.PostAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.PostPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Post;

import java.util.List;

public interface PostManageService {
    Page<Post> page(PostPageDTO dto);

    Post add(PostAddDTO dto);

    void update(Integer id, PostAddDTO dto);

    void updateStatus(Integer id, Integer status);

    void delete(List<Integer> ids);
}
