package com.it.zwx.studenthub_system.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("study_check")
public class StudyCheck {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer studentId;
    private Integer counselorId;
    private String classNo;
    private String content;
    private String location;
    private String imageUrl;
    private Integer status; // 0异常 1正常
    private LocalDateTime checkTime;
    @TableField(exist = false)
    private String studentName;
    @TableField(exist = false)
    private String className;
}

