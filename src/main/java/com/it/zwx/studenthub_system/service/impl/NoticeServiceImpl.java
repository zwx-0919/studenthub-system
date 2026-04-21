package com.it.zwx.studenthub_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.it.zwx.studenthub_system.exception.BusinessException;
import com.it.zwx.studenthub_system.mapper.NoticeMapper;
import com.it.zwx.studenthub_system.pojo.dto.NoticeDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.NoticePageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Notice;
import com.it.zwx.studenthub_system.service.NoticeService;
import com.it.zwx.studenthub_system.service.RedisCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    private static final String NOTICE_CACHE_PATTERN = "cache:notice:*";
    private static final String NOTICE_PAGE_CACHE_PREFIX = "cache:notice:page:";

    @Autowired
    private RedisCacheService redisCacheService;

    private void evictNoticeCaches() {
        redisCacheService.deleteCacheByPattern(NOTICE_CACHE_PATTERN);
    }

    /**
     * 条件查询公告列表
     *
     * @param dto
     * @return
     */
    @Override
    public Page<Notice> listNotice(NoticePageDTO dto) {
        long current = dto.getCurrent() == null || dto.getCurrent() < 1 ? 1 : dto.getCurrent();
        long size = dto.getSize() == null || dto.getSize() < 1 ? 10 : dto.getSize();

        String cacheKey = NOTICE_PAGE_CACHE_PREFIX
                + "c=" + current
                + "&s=" + size
                + "&title=" + (dto.getTitleKeyword() == null ? "" : dto.getTitleKeyword())
                + "&status=" + (dto.getStatus() == null ? "" : dto.getStatus())
                + "&authorId=" + (dto.getAuthorId() == null ? "" : dto.getAuthorId())
                + "&start=" + (dto.getPublishTimeStart() == null ? "" : dto.getPublishTimeStart())
                + "&end=" + (dto.getPublishTimeEnd() == null ? "" : dto.getPublishTimeEnd());
        Page<Notice> cached = redisCacheService.getCache(cacheKey, new TypeReference<Page<Notice>>() {});
        if (cached != null) {
            return cached;
        }

        Page<Notice> page = new Page<>(current, size);

        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(dto.getTitleKeyword() != null, "title", dto.getTitleKeyword())
                .eq(dto.getStatus() != null, "status", dto.getStatus())
                .eq(dto.getAuthorId() != null, "author_id", dto.getAuthorId())
                .ge(dto.getPublishTimeStart() != null, "publish_time", dto.getPublishTimeStart())
                .le(dto.getPublishTimeEnd() != null, "publish_time", dto.getPublishTimeEnd());

        baseMapper.selectPage(page, queryWrapper);
        redisCacheService.setCache(cacheKey, page, 5);
        return page;
    }

    /**
     * 新增公告
     *
     * @param dto
     */
    @Override
    public void addNotice(NoticeDTO dto) {

        Notice notice = new Notice();

        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setImageUrl(dto.getImageUrl());
        notice.setAuthorId(dto.getAuthorId());
        notice.setAuthorName(dto.getAuthorName());
        notice.setStatus(1);//默认已发布

        notice.setPublishTime(dto.getPublishTime() == null ? LocalDateTime.now() : dto.getPublishTime());

        notice.setViewCount(0);

        baseMapper.insert(notice);
        evictNoticeCaches();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNotice(Integer id, NoticeDTO dto) {

        Notice notice = baseMapper.selectById(id);
        if (notice == null) {
            throw new IllegalArgumentException("公告不存在");
        }

        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setImageUrl(dto.getImageUrl());
        notice.setAuthorId(dto.getAuthorId());
        notice.setAuthorName(dto.getAuthorName());
        notice.setStatus(dto.getStatus());
        notice.setUpdateTime(LocalDateTime.now());

        if (dto.getViewCount() != null) {
            notice.setViewCount(dto.getViewCount());
        }

        baseMapper.updateById(notice);
        evictNoticeCaches();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNoticeStatus(Integer id) {
        UpdateWrapper<Notice> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .in("status", 0, 1);

        // 2. 正确拼接CASE语句（用setSql，直接执行SQL片段）
        updateWrapper.setSql("status = CASE WHEN status=0 THEN 1 ELSE 0 END");

        // 3. 执行更新并校验行数
        int rows = baseMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("公告不存在，或当前状态不支持切换（仅已发布/已下架可切换）");
        }
        evictNoticeCaches();
    }

    @Override
    public void deleteNotice(List<Integer> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的公告");
        }
        // 1. 数据库批量删除
        int deleteCount = baseMapper.deleteByIds(ids);
        if (deleteCount == 0) {
            throw new BusinessException("公告删除失败，部分公告可能已不存在");
        }
        log.info("管理员批量删除公告：成功删除{}个公告，ID列表={}", deleteCount, ids);
        evictNoticeCaches();
    }

    @Override
    public Notice detailAndIncrView(Integer id) {
        Notice notice = baseMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        Notice update = new Notice();
        update.setId(id);
        update.setViewCount((notice.getViewCount() == null ? 0 : notice.getViewCount()) + 1);
        baseMapper.updateById(update);
        evictNoticeCaches();
        return baseMapper.selectById(id);
    }
}

