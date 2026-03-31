package com.it.zwx.studenthub_system.pojo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试新增/更新入参
 */
@Data
@Builder
public class ExamDTO {
    private Integer id; // 修改时传

    private Integer courseId;

    private String examName;

    private LocalDateTime time;

    private String location;

    private LocalDateTime remindTime;

    private String classNo;
    private Integer majorId;

    private Integer status;
}

