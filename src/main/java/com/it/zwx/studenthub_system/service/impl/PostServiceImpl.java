package com.it.zwx.studenthub_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.zwx.studenthub_system.exception.BusinessException;
import com.it.zwx.studenthub_system.mapper.PostCommentMapper;
import com.it.zwx.studenthub_system.mapper.PostLikeMapper;
import com.it.zwx.studenthub_system.mapper.PostMapper;
import com.it.zwx.studenthub_system.pojo.dto.InteractionRemindDTO;
import com.it.zwx.studenthub_system.pojo.dto.PostAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.PostCommentAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.PostPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Post;
import com.it.zwx.studenthub_system.pojo.entity.PostComment;
import com.it.zwx.studenthub_system.pojo.entity.PostLike;
import com.it.zwx.studenthub_system.pojo.entity.User;
import com.it.zwx.studenthub_system.pojo.vo.PostDetailVO;
import com.it.zwx.studenthub_system.pojo.vo.PostLikeResultVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.it.zwx.studenthub_system.service.MessageService;
import com.it.zwx.studenthub_system.service.PostService;
import com.it.zwx.studenthub_system.service.RedisCacheService;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private static final String CACHE_POST_PAGE_PATTERN = "cache:post:page:*";
    private static final String CACHE_POST_PAGE_PREFIX = "cache:post:page:published:";

    private final UserService userService;
    private final PostCommentMapper postCommentMapper;
    private final PostLikeMapper postLikeMapper;
    private final MessageService messageService;
    private final RedisCacheService redisCacheService;

    private void evictPostCaches() {
        redisCacheService.deleteCacheByPattern(CACHE_POST_PAGE_PATTERN);
    }

    @Override
    public boolean updateById(Post entity) {
        boolean ok = super.updateById(entity);
        if (ok) {
            evictPostCaches();
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        evictPostCaches();
        return post;
    }

    @Override
    public Page<Post> pagePublished(PostPageDTO dto) {
        long current = dto.getCurrent() == null || dto.getCurrent() < 1 ? 1 : dto.getCurrent();
        long size = dto.getSize() == null || dto.getSize() < 1 ? 10 : dto.getSize();

        String cacheKey = CACHE_POST_PAGE_PREFIX
                + "c=" + current
                + "&s=" + size
                + "&cat=" + (dto.getCategory() == null ? "" : dto.getCategory())
                + "&kw=" + (dto.getKeyword() == null ? "" : dto.getKeyword());
        Page<Post> cached = redisCacheService.getCache(cacheKey, new TypeReference<Page<Post>>() {});
        if (cached != null) {
            // liked 字段是“当前用户”维度，缓存里的 liked 不能复用，这里每次重新填充
            fillLikedForPosts(cached.getRecords());
            return cached;
        }

        Page<Post> page = new Page<>(current, size);
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("status", 1)
                .eq(dto.getCategory() != null && !dto.getCategory().isBlank(), "category", dto.getCategory())
                .and(dto.getKeyword() != null && !dto.getKeyword().isBlank(),
                        w -> w.like("title", dto.getKeyword()).or().like("content", dto.getKeyword()))
                .orderByDesc("create_time");
        baseMapper.selectPage(page, qw);
        fillLikedForPosts(page.getRecords());

        // 写缓存前把 liked 置空（避免把某个用户的 liked 状态缓存成全局）
        if (page.getRecords() != null) {
            page.getRecords().forEach(p -> p.setLiked(null));
        }
        redisCacheService.setCache(cacheKey, page, 5);

        // 返回给调用方仍需要 liked（重新填充一次）
        fillLikedForPosts(page.getRecords());
        return page;
    }

    private void fillLikedForPosts(List<Post> posts) {
        if (posts == null || posts.isEmpty()) return;
        Integer uid = tryCurrentUserId();
        if (uid == null) {
            posts.forEach(p -> p.setLiked(false));
            return;
        }
        List<Integer> ids = posts.stream().map(Post::getId).filter(Objects::nonNull).collect(Collectors.toList());
        if (ids.isEmpty()) return;
        List<PostLike> likes = postLikeMapper.selectList(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getUserId, uid)
                .in(PostLike::getPostId, ids));
        Set<Integer> liked = likes.stream().map(PostLike::getPostId).collect(Collectors.toSet());
        posts.forEach(p -> p.setLiked(liked.contains(p.getId())));
    }

    private Integer tryCurrentUserId() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null || claims.get("id") == null) return null;
        return claims.get("id") instanceof Integer ? (Integer) claims.get("id") : ((Number) claims.get("id")).intValue();
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
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Integer id, Integer status) {
        Post post = baseMapper.selectById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        if (status != null && (status == 0 || status == 1)) {
            post.setStatus(status);
            baseMapper.updateById(post);
            evictPostCaches();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) throw new BusinessException("请选择要删除的帖子");
        baseMapper.deleteBatchIds(ids);
        evictPostCaches();
    }

    @Override
    public PostDetailVO detail(Integer id) {
        Post post = baseMapper.selectById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        QueryWrapper<PostComment> commentQw = new QueryWrapper<>();
        commentQw.eq("post_id", id).orderByAsc("create_time");
        List<PostComment> raw = postCommentMapper.selectList(commentQw);
        List<PostComment> comments = orderAndEnrichComments(raw);
        PostDetailVO vo = new PostDetailVO();
        vo.setPost(post);
        vo.setComments(comments);
        Integer uid = tryCurrentUserId();
        if (uid != null) {
            Long cnt = postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                    .eq(PostLike::getPostId, id).eq(PostLike::getUserId, uid));
            post.setLiked(cnt != null && cnt > 0);
        } else {
            post.setLiked(false);
        }
        return vo;
    }

    private List<PostComment> orderAndEnrichComments(List<PostComment> raw) {
        if (raw == null || raw.isEmpty()) return raw;
        Set<Integer> userIds = new HashSet<>();
        for (PostComment c : raw) {
            userIds.add(c.getUserId());
            if (c.getReplyToUserId() != null) userIds.add(c.getReplyToUserId());
        }
        Map<Integer, String> names = new HashMap<>();
        for (Integer uid : userIds) {
            User u = userService.getById(uid);
            if (u != null) names.put(uid, u.getUserName());
        }
        for (PostComment c : raw) {
            if (c.getReplyToUserId() != null) {
                c.setReplyToUserName(names.get(c.getReplyToUserId()));
            }
        }
        List<PostComment> top = raw.stream()
                .filter(c -> c.getParentId() == null)
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .collect(Collectors.toList());
        List<PostComment> ordered = new ArrayList<>();
        for (PostComment t : top) {
            ordered.add(t);
            raw.stream()
                    .filter(c -> t.getId().equals(c.getParentId()))
                    .sorted((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()))
                    .forEach(ordered::add);
        }
        return ordered;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        if (dto.getParentId() != null) {
            PostComment parent = postCommentMapper.selectById(dto.getParentId());
            if (parent == null || !parent.getPostId().equals(dto.getPostId())) {
                throw new BusinessException("回复的评论不存在");
            }
            comment.setParentId(parent.getId());
            comment.setReplyToUserId(parent.getUserId());
        }
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
        evictPostCaches();
        return comment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostLikeResultVO toggleLike(Integer postId) {
        User user = currentUser();
        Post post = baseMapper.selectById(postId);
        if (post == null) throw new BusinessException("帖子不存在");
        LambdaQueryWrapper<PostLike> qw = new LambdaQueryWrapper<>();
        qw.eq(PostLike::getPostId, postId).eq(PostLike::getUserId, user.getId());
        PostLike exist = postLikeMapper.selectOne(qw);
        boolean nowLiked;
        if (exist != null) {
            postLikeMapper.deleteById(exist.getId());
            UpdateWrapper<Post> uw = new UpdateWrapper<>();
            uw.eq("id", postId).setSql("like_count = GREATEST(like_count - 1, 0)");
            baseMapper.update(null, uw);
            nowLiked = false;
        } else {
            PostLike pl = new PostLike();
            pl.setPostId(postId);
            pl.setUserId(user.getId());
            pl.setCreateTime(LocalDateTime.now());
            postLikeMapper.insert(pl);
            UpdateWrapper<Post> uw = new UpdateWrapper<>();
            uw.eq("id", postId).setSql("like_count = like_count + 1");
            baseMapper.update(null, uw);
            nowLiked = true;
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
        post = baseMapper.selectById(postId);
        evictPostCaches();
        return new PostLikeResultVO(nowLiked, post.getLikeCount() == null ? 0 : post.getLikeCount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMine(Integer postId) {
        User user = currentUser();
        Post post = baseMapper.selectById(postId);
        if (post == null) throw new BusinessException("帖子不存在");
        if (!user.getId().equals(post.getUserId())) {
            throw new BusinessException("只能删除自己发布的帖子");
        }
        postLikeMapper.delete(new LambdaQueryWrapper<PostLike>().eq(PostLike::getPostId, postId));
        baseMapper.deleteById(postId);
        postCommentMapper.delete(new QueryWrapper<PostComment>().eq("post_id", postId));
        evictPostCaches();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Integer commentId) {
        User user = currentUser();
        PostComment c = postCommentMapper.selectById(commentId);
        if (c == null) throw new BusinessException("评论不存在");
        Post post = baseMapper.selectById(c.getPostId());
        if (post == null) throw new BusinessException("帖子不存在");
        boolean owner = user.getId().equals(c.getUserId());
        boolean postOwner = user.getId().equals(post.getUserId());
        if (!owner && !postOwner) throw new BusinessException("无权删除该评论");
        List<Integer> toDelete = new ArrayList<>();
        toDelete.add(commentId);
        postCommentMapper.selectList(new LambdaQueryWrapper<PostComment>().eq(PostComment::getParentId, commentId))
                .forEach(reply -> toDelete.add(reply.getId()));
        postCommentMapper.deleteBatchIds(toDelete);
        int n = toDelete.size();
        UpdateWrapper<Post> uw = new UpdateWrapper<>();
        uw.eq("id", post.getId()).setSql("comment_count = GREATEST(comment_count - " + n + ", 0)");
        baseMapper.update(null, uw);
        evictPostCaches();
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
