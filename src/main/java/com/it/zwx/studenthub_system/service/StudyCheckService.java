package com.it.zwx.studenthub_system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.it.zwx.studenthub_system.pojo.dto.StudyCheckAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.StudyCheckPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.StudyCheck;

import java.util.List;
import java.util.Map;

public interface StudyCheckService extends IService<StudyCheck> {
    void addCheck(StudyCheckAddDTO dto);
    Page<StudyCheck> studentPage(StudyCheckPageDTO dto);
    Page<StudyCheck> counselorPage(StudyCheckPageDTO dto);
    double classCheckRate(Integer classId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    java.util.List<com.it.zwx.studenthub_system.pojo.entity.ClassInfo> listClasses();

    /** 学生端：本周/本月是否未达标 */
    Map<String, Object> studentCheckWarning();

    /** 辅导员：未达标学生预警名单 */
    List<Map<String, Object>> counselorWarningStudents();
}

