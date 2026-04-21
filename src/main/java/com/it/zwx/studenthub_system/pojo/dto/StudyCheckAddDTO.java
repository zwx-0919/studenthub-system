package com.it.zwx.studenthub_system.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class StudyCheckAddDTO {
    @NotBlank(message = "打卡内容不能为空")
    private String content;
    @NotBlank(message = "打卡位置不能为空")
    private String location;
    private MultipartFile image;
    /** 本次学习时长（分钟），0–720 */
    @Min(value = 0, message = "学习时长不能为负数")
    @Max(value = 720, message = "单次学习时长不能超过720分钟")
    private Integer studyDurationMinutes;
}

