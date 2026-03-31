package com.it.zwx.studenthub_system.pojo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程新增/更新入参
 */
@Data
@Builder
public class CourseDTO {
    private Integer id; // 修改时传

    private String courseName;

    private String courseNumber;

    private String teacherName;

    private LocalDateTime time;

    private String location;

    private String term;

    private String classNo;
    private Integer majorId;

    private Integer status;
}

