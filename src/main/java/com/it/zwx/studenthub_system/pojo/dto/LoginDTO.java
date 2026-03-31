package com.it.zwx.studenthub_system.pojo.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String userAccount;
    private String password;
    private Integer role;
}
