package com.it.zwx.studenthub_system.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notice")
public class Notice {
    @TableId(type = IdType.AUTO)
    private Integer id; // 公告ID
    private String title; // 公告标题
    private String content; // 公告内容
    private String imageUrl; // 图片URL（多个用逗号分隔）
    private Integer authorId; // 发布人ID
    private String authorName; // 发布人姓名
    private Integer status; // 状态：0=已下架，1=已发布
    private Integer viewCount; // 浏览次数
    private LocalDateTime publishTime; // 发布时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间
}