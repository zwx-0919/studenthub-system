package com.it.zwx.studenthub_system.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("leave_apply")
public class LeaveApply {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer studentId;
    private Integer counselorId;
    private String classNo;
    private Integer leaveType; // 1事假 2病假 3其他
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;
    private String reason;
    private String proofUrl;
    private Integer approveStatus; // 0待审批 1通过 2驳回
    private String approveRemark;
    private LocalDateTime approveTime;
    private LocalDateTime createTime;
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(exist = false)
    private String studentName;
    @TableField(exist = false)
    private String className;
}

