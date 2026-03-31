package com.it.zwx.studenthub_system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.it.zwx.studenthub_system.pojo.dto.AiConsultAskDTO;
import com.it.zwx.studenthub_system.pojo.dto.base.PageDTO;
import com.it.zwx.studenthub_system.pojo.entity.AiConsult;

public interface AiConsultService extends IService<AiConsult> {
    AiConsult ask(AiConsultAskDTO dto);
    Page<AiConsult> myPage(PageDTO dto);
}

