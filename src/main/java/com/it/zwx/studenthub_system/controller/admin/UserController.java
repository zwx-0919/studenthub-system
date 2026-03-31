package com.it.zwx.studenthub_system.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.constant.MessageConstant;
import com.it.zwx.studenthub_system.pojo.dto.UserAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.UserPageDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.UserStatusDTO;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.pojo.entity.User;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController("adminUserController")
@RequestMapping("/admin/user")
@Tag(name = "管理员用户管理")
@CrossOrigin
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 查看用户列表
     * @return
     */
    @GetMapping
    @Operation(summary = "查看用户列表")
    public Result<Page<User>> listUser(@ModelAttribute UserPageDTO userPageDTO){
        AuthUtil.requireAdmin(AuthUtil.currentUser(userService));
        log.info("查看用户列表");
        Page<User> page = userService.listUser(userPageDTO);
        return Result.success(page);
    }

    @PostMapping("/add")
    @Operation(summary = "新增用户")
    public Result addUser(@RequestBody UserAddDTO dto) {
        AuthUtil.requireAdmin(AuthUtil.currentUser(userService));
        userService.addUser(dto);
        return Result.success(MessageConstant.ADD_SUCCESS);
    }

    /**
     * 更新用户状态
     * @param statusDTO
     * @return
     */
    @PatchMapping("/status")
    @Operation(summary = "更新用户状态")
    public Result updateUserStatus(@RequestBody UserStatusDTO statusDTO) {
        AuthUtil.requireAdmin(AuthUtil.currentUser(userService));
        log.info("更新用户状态: statusDTO={}", statusDTO);

        userService.updateUserStatus(statusDTO);
        return Result.success(MessageConstant.UPDATE_SUCCESS);
    }

    /**
     * 删除用户
     * @param
     * @return
     */
    @DeleteMapping
    @Operation(summary = "删除用户")
    public Result deleteUser(@RequestBody List<Integer> ids){
        AuthUtil.requireAdmin(AuthUtil.currentUser(userService));
        log.info("删除用户");
        userService.deleteUser(ids);
        return Result.success(MessageConstant.DELETE_SUCCESS);
    }

    /**
     * 查看用户信息
     * @param userId
     * @return
     */
    @GetMapping("/info")
    @Operation(summary = "查看用户信息")
    public Result<User> getUserInfo( @RequestParam Integer userId){
        AuthUtil.requireAdmin(AuthUtil.currentUser(userService));
        log.info("查看用户信息");
        User user = userService.getById(userId);
        return Result.success(user);
    }

}
