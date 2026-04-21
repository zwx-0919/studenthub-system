package com.it.zwx.studenthub_system.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_consult")
public class AiConsult {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer studentId;
    @TableField(exist = false)
    private String sessionId;
    private String question;
    private String answer;
    @TableField(exist = false)
    private String category;
    @TableField(exist = false)
    private String contextSummary;
    private LocalDateTime createTime;
}

