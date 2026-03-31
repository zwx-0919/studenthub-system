package com.it.zwx.studenthub_system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.it.zwx.studenthub_system.pojo.dto.NoticeDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.NoticePageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Notice;

import java.util.List;

public interface NoticeService extends IService<Notice>{
    Page<Notice> listNotice(NoticePageDTO dto);

    void addNotice(NoticeDTO dto);

    void updateNotice(Integer id, NoticeDTO dto);

    void updateNoticeStatus(Integer id);

    void deleteNotice(List<Integer> ids);

    Notice detailAndIncrView(Integer id);
}
