package com.it.zwx.studenthub_system.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostAddDTO {
    @NotBlank(message = "分类不能为空")
    private String category;

    @NotBlank(message = "标题不能为空")
    @Size(min = 5, max = 30, message = "标题长度需在5到30之间")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(min = 10, max = 300, message = "内容长度需在10到300之间")
    private String content;

    @Size(max = 500, message = "图片地址长度不能超过500")
    private String imageUrl;
}

