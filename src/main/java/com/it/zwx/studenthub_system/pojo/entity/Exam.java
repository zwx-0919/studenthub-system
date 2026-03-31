package com.it.zwx.studenthub_system.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam")
public class Exam {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer courseId; // 对应课程ID

    private String examName; // 考试名称

    private LocalDateTime time; // 考试时间

    private String location; // 地点

    private LocalDateTime remindTime; // 提醒时间

    private String classNo; // 班级编号
    private Integer majorId; // 专业ID

    private Integer status; // 状态：0=禁用，1=启用

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间（如表存在）
}

