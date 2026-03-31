package com.it.zwx.studenthub_system.pojo.dto.extend;

import com.it.zwx.studenthub_system.pojo.dto.base.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 考试分页查询参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExamPageDTO extends PageDTO {
    private Integer status; // 管理员筛选
    private Integer courseId; // 按课程查
    private String classNo; // 班级筛选
    private Integer majorId; // 专业
    private String examNameKeyword; // 考试名称模糊
    private LocalDateTime timeStart; // 考试时间起
    private LocalDateTime timeEnd; // 考试时间止
    private LocalDateTime remindTimeStart; // 提醒时间起
    private LocalDateTime remindTimeEnd; // 提醒时间止
}

