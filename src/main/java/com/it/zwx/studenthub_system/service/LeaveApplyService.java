package com.it.zwx.studenthub_system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.it.zwx.studenthub_system.pojo.dto.LeaveApplyDTO;
import com.it.zwx.studenthub_system.pojo.dto.LeaveApproveDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.LeaveApplyPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.LeaveApply;

public interface LeaveApplyService extends IService<LeaveApply> {
    void submit(LeaveApplyDTO dto);
    void approve(Integer id, LeaveApproveDTO dto);
    Page<LeaveApply> studentPage(LeaveApplyPageDTO dto);
    Page<LeaveApply> counselorPage(LeaveApplyPageDTO dto);
}

