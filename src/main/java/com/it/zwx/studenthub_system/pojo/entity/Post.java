package com.it.zwx.studenthub_system.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String userName;
    private String category;
    private String title;
    private String content;
    private String imageUrl;
    private Integer status; //0=已下架，1=已发布
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 当前用户是否已点赞（仅查询时填充，非表字段） */
    @TableField(exist = false)
    private Boolean liked;
}

