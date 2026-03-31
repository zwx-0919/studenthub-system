package com.it.zwx.studenthub_system.pojo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NoticeDTO {
    private Integer id; // 修改传，新增不传
    private String title;
    private String content;
    private String imageUrl;
    private Integer authorId;
    private String authorName;
    private Integer status;
    private LocalDateTime publishTime;// 发布时间
    private LocalDateTime updateTime;// 更新时间
    private Integer viewCount = 0;// 查看次数
}
