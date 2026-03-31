package com.it.zwx.studenthub_system.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String courseName; // 课程名称

    private String courseNumber; // 课程编号

    private String teacherName; // 授课老师

    private LocalDateTime time; // 上课时间

    private String location; // 上课教室

    private String term; // 学期

    private String classNo; // 班级编号
    private Integer majorId; // 专业ID

    private Integer status; // 状态：0=禁用，1=启用

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间（如果表没建字段也不会影响编译）
}

