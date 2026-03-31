package com.it.zwx.studenthub_system.pojo.dto;

import lombok.Data;

@Data
public class UserAddDTO {
    private String userAccount;
    private String userName;
    private String password;
    private Integer identityType;
    private Integer classId;
    private String chargeClassIds;
    private String chargeMajorIds;
    private String email;
    private String userAvatar;
    private Integer status;
}
