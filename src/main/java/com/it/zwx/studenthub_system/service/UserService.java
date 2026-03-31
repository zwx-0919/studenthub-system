package com.it.zwx.studenthub_system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.it.zwx.studenthub_system.pojo.dto.LoginDTO;
import com.it.zwx.studenthub_system.pojo.dto.ResetPwdByEmailDTO;
import com.it.zwx.studenthub_system.pojo.dto.UserAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.UserPageDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.UserStatusDTO;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.pojo.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.Map;


public interface UserService extends IService<User> {

    User getByUserName(String userAccount);

    Map<String, Object> login(LoginDTO loginDTO);

    Page<User> listUser(UserPageDTO userPageDTO);

    void updateUserStatus(UserStatusDTO statusDTO);

    void deleteUser(List<Integer> ids);

    User getByEmail(String email);

    void resetPwdByEmail(ResetPwdByEmailDTO dto);

    void bindEmail(Integer id, @NotBlank(message = "邮箱不能为空") @Pattern(regexp = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "邮箱格式错误") String email);

    void addUser(UserAddDTO dto);
}
