package com.it.zwx.studenthub_system.pojo.dto.extend;

import com.it.zwx.studenthub_system.pojo.dto.base.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询DTO参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserPageDTO extends PageDTO {
    /**
     * 用户的帐号
     */
    private String userAccount;
    /**
     * 用户的名称
     */
    private String userName;
    /**
     * 用户的角色
     */
    private Integer role;
    /**
     * 是否被禁言
     */
    private Integer status;
    /**
     * 班级ID（学生筛选）
     */
    private Integer classId;
}
