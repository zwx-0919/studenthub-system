package com.it.zwx.studenthub_system.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class StudyCheckAddDTO {
    @NotBlank(message = "打卡内容不能为空")
    private String content;
    @NotBlank(message = "打卡位置不能为空")
    private String location;
    private MultipartFile image;
}

