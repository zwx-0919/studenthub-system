package com.it.zwx.studenthub_system.pojo.dto.extend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserStatusDTO {
    /**
     * 用户ID
     */
    private Integer id;

    /**
     * 状态 (0:禁用, 1:正常)
     */
    private Integer status;
}
