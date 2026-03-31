package com.it.zwx.studenthub_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.zwx.studenthub_system.exception.BusinessException;
import com.it.zwx.studenthub_system.mapper.PostCommentMapper;
import com.it.zwx.studenthub_system.mapper.PostMapper;
import com.it.zwx.studenthub_system.pojo.dto.InteractionRemindDTO;
import com.it.zwx.studenthub_system.pojo.dto.PostAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.PostCommentAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.PostPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Post;
import com.it.zwx.studenthub_system.pojo.entity.PostComment;
import com.it.zwx.studenthub_system.pojo.entity.User;
import com.it.zwx.studenthub_system.pojo.vo.PostDetailVO;
import com.it.zwx.studenthub_system.service.MessageService;
import com.it.zwx.studenthub_system.service.PostService;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final UserService userService;
    private final PostCommentMapper postCommentMapper;
    private final MessageService messageService;

    @Override
    public Post add(PostAddDTO dto) {
        User user = currentUser();
        assertCanPublish(user);
        Post post = new Post();
        post.setUserId(user.getId());
        post.setUserName(user.getUserName());
        post.setCategory(dto.getCategory());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setImageUrl(dto.getImageUrl());
        post.setStatus(1);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(post);
        return post;
    }

    @Override
    public Page<Post> pagePublished(PostPageDTO dto) {
        long current = dto.getCurrent() == null || dto.getCurrent() < 1 ? 1 : dto.getCurrent();
        long size = dto.getSize() == null || dto.getSize() < 1 ? 10 : dto.getSize();
        Page<Post> page = new Page<>(current, size);
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("status", 1)
                .eq(dto.getCategory() != null && !dto.getCategory().isBlank(), "category", dto.getCategory())
                .and(dto.getKeyword() != null && !dto.getKeyword().isBlank(),
                        w -> w.like("title", dto.getKeyword()).or().like("content", dto.getKeyword()))
                .orderByDesc("create_time");
        baseMapper.selectPage(page, qw);
        return page;
    }

    @Override
    public Page<Post> adminPage(PostPageDTO dto) {
        long current = dto.getCurrent() == null || dto.getCurrent() < 1 ? 1 : dto.getCurrent();
        long size = dto.getSize() == null || dto.getSize() < 1 ? 10 : dto.getSize();
        Page<Post> page = new Page<>(current, size);
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq(dto.getStatus() != null, "status", dto.getStatus())
                .eq(dto.getCategory() != null && !dto.getCategory().isBlank(), "category", dto.getCategory())
                .and(dto.getKeyword() != null && !dto.getKeyword().isBlank(),
                        w -> w.like("title", dto.getKeyword()).or().like("content", dto.getKeyword()))
                .orderByDesc("create_time");
        baseMapper.selectPage(page, qw);
        return page;
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        Post post = baseMapper.selectById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        if (status != null && (status == 0 || status == 1)) {
            post.setStatus(status);
            baseMapper.updateById(post);
        }
    }

    @Override
    public void deleteByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) throw new BusinessException("请选择要删除的帖子");
        baseMapper.deleteBatchIds(ids);
    }

    @Override
    public PostDetailVO detail(Integer id) {
        Post post = baseMapper.selectById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        QueryWrapper<PostComment> commentQw = new QueryWrapper<>();
        commentQw.eq("post_id", id).orderByDesc("create_time");
        List<PostComment> comments = postCommentMapper.selectList(commentQw);
        PostDetailVO vo = new PostDetailVO();
        vo.setPost(post);
        vo.setComments(comments);
        return vo;
    }

    @Override
    public PostComment addComment(PostCommentAddDTO dto) {
        User user = currentUser();
        assertCanPublish(user);
        Post post = baseMapper.selectById(dto.getPostId());
        if (post == null) throw new BusinessException("帖子不存在");
        PostComment comment = new PostComment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(user.getId());
        comment.setContent(dto.getContent());
        comment.setCreateTime(LocalDateTime.now());
        postCommentMapper.insert(comment);

        UpdateWrapper<Post> uw = new UpdateWrapper<>();
        uw.eq("id", post.getId()).setSql("comment_count = comment_count + 1");
        baseMapper.update(null, uw);

        if (!user.getId().equals(post.getUserId())) {
            InteractionRemindDTO remind = new InteractionRemindDTO();
            remind.setReceiverId(post.getUserId());
            remind.setActorName(user.getUserName());
            remind.setAction("评论");
            remind.setPostTitle(post.getTitle());
            remind.setBizId(post.getId());
            messageService.pushInteraction(remind);
        }
        return comment;
    }

    @Override
    public void like(Integer postId) {
        User user = currentUser();
        Post post = baseMapper.selectById(postId);
        if (post == null) throw new BusinessException("帖子不存在");
        UpdateWrapper<Post> uw = new UpdateWrapper<>();
        uw.eq("id", postId).setSql("like_count = like_count + 1");
        baseMapper.update(null, uw);
        if (!user.getId().equals(post.getUserId())) {
            InteractionRemindDTO remind = new InteractionRemindDTO();
            remind.setReceiverId(post.getUserId());
            remind.setActorName(user.getUserName());
            remind.setAction("点赞");
            remind.setPostTitle(post.getTitle());
            remind.setBizId(post.getId());
            messageService.pushInteraction(remind);
        }
    }

    @Override
    public void deleteMine(Integer postId) {
        User user = currentUser();
        Post post = baseMapper.selectById(postId);
        if (post == null) throw new BusinessException("帖子不存在");
        if (!user.getId().equals(post.getUserId())) {
            throw new BusinessException("只能删除自己发布的帖子");
        }
        baseMapper.deleteById(postId);
        postCommentMapper.delete(new QueryWrapper<PostComment>().eq("post_id", postId));
    }

    private User currentUser() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null || claims.get("id") == null) {
            throw new BusinessException("用户未登录");
        }
        Integer uid = claims.get("id") instanceof Integer
                ? (Integer) claims.get("id")
                : ((Number) claims.get("id")).intValue();
        User user = userService.getById(uid);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private void assertCanPublish(User user) {
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("您已被禁言，无法发布内容");
        }
    }
}

