package com.it.zwx.studenthub_system.pojo.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    @Email(message = "邮箱格式不正确")
    private String email;
    private String userAvatar;
}
