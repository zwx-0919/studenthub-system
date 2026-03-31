package com.it.zwx.studenthub_system.utils;

import com.it.zwx.studenthub_system.constant.RoleConstant;
import com.it.zwx.studenthub_system.exception.BusinessException;
import com.it.zwx.studenthub_system.pojo.entity.User;
import com.it.zwx.studenthub_system.service.UserService;

import java.util.Map;

public class AuthUtil {
    private AuthUtil() {
    }

    public static User currentUser(UserService userService) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null || claims.get("id") == null) {
            throw new BusinessException("用户未登录");
        }
        Integer uid = claims.get("id") instanceof Integer ? (Integer) claims.get("id") : ((Number) claims.get("id")).intValue();
        User user = userService.getById(uid);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    public static void requireAdmin(User user) {
        Integer role = user.getIdentityType() == null ? user.getRole() : user.getIdentityType();
        if (role == null || role != RoleConstant.ADMIN) {
            throw new BusinessException("无管理员权限");
        }
    }

    public static void requireCounselor(User user) {
        Integer role = user.getIdentityType() == null ? user.getRole() : user.getIdentityType();
        if (role == null || role != RoleConstant.TEACHER) {
            throw new BusinessException("无辅导员权限");
        }
    }
}
