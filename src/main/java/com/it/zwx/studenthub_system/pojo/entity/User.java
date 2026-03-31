package com.it.zwx.studenthub_system.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private Integer id;
    private String userAccount;//学号
    private String userName;//昵称
    private String password;//密码
    private String userAvatar;//头像
    private String email;//邮箱
    private Integer role;//角色 1-学生 2-辅导员 3-管理员
    private Integer classId;//学生所属班级ID
    private Integer counselorId;//学生对应辅导员ID
    private String chargeClassIds;//辅导员负责班级ID列表，逗号分隔
    private String chargeMajorIds;//辅导员负责专业ID列表，逗号分隔
    private Integer identityType;//身份类型 1-学生 2-辅导员 3-管理员
    private Integer status;//禁言状态 0-禁言 1-正常
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;//最后登录时间
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;//创建时间
}
