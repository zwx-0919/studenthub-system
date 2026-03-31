package com.it.zwx.studenthub_system.pojo.dto.extend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {
    // 昵称（可选）
    private String nickname;

    // 邮箱（可选）
    private String email;

    /// 头像
    private String avatar;

    // 验证码（更新邮箱时需要）
    private String code;
}