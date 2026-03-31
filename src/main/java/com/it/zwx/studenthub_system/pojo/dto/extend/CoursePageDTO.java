package com.it.zwx.studenthub_system.pojo.dto.extend;

import com.it.zwx.studenthub_system.pojo.dto.base.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 课程分页查询参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CoursePageDTO extends PageDTO {
    private Integer status; // 状态筛选（管理员用）
    private String courseNameKeyword; // 课程名称模糊
    private String teacherNameKeyword; // 授课老师模糊
    private String term; // 学期
    private String classNo; // 班级
    private Integer majorId; // 专业
    private LocalDateTime timeStart; // 上课时间起
    private LocalDateTime timeEnd; // 上课时间止
}

