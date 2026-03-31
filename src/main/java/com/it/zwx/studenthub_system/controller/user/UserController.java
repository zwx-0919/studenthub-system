package com.it.zwx.studenthub_system.controller.user;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.it.zwx.studenthub_system.constant.MessageConstant;
import com.it.zwx.studenthub_system.exception.BusinessException;
import com.it.zwx.studenthub_system.pojo.dto.BindEmailDTO;
import com.it.zwx.studenthub_system.pojo.dto.LoginDTO;
import com.it.zwx.studenthub_system.pojo.dto.ResetPwdByEmailDTO;
import com.it.zwx.studenthub_system.pojo.dto.UserPasswordUpdateDTO;
import com.it.zwx.studenthub_system.pojo.dto.UserProfileUpdateDTO;
import com.it.zwx.studenthub_system.pojo.entity.ClassInfo;
import com.it.zwx.studenthub_system.pojo.entity.MajorInfo;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.pojo.entity.User;
import com.it.zwx.studenthub_system.service.ClassInfoService;
import com.it.zwx.studenthub_system.service.MajorInfoService;
import com.it.zwx.studenthub_system.service.ReminderService;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.AliOssUtils;
import com.it.zwx.studenthub_system.utils.EmailUtils;
import com.it.zwx.studenthub_system.utils.IdFactoryUtil;
import com.it.zwx.studenthub_system.utils.Md5Util;
import com.it.zwx.studenthub_system.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "普通用户管理")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private ClassInfoService classInfoService;
    @Autowired
    private ReminderService reminderService;
    @Autowired
    private MajorInfoService majorInfoService;
    @Autowired
    private EmailUtils emailUtils;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private Md5Util md5Util;

    /**
     * 用户登录
     *
     * @param loginDTO
     * @return
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result login(@RequestBody LoginDTO loginDTO) {
        log.info("用户登录：{}", loginDTO);
        try {
            Map<String, Object> loginResult = userService.login(loginDTO);
            Object userObj = loginResult.get("user");
            if (userObj instanceof User user && user.getRole() != null && user.getRole() == 1) {
                // 学生登录后推送「24小时课程 + 7天考试」提醒（WebSocket在线时实时可见）
                reminderService.pushLoginReminders(user.getId());
            }

            return Result.success(loginResult);

        } catch (BusinessException e) {
            log.error("用户登录失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("用户登录系统异常", e);
            return Result.error("用户登录失败");
        }
    }

    /**
     * 发送邮箱验证码
     *
     * @param email
     * @return
     */
    @PostMapping("/send-email-code")
    @Operation(summary = "发送邮箱验证码")
    public Result sendEmailCode(@RequestParam String email) {
        // 1. 校验用户
        User user = userService.getByEmail(email);
        if (user == null) throw new BusinessException("该邮箱未绑定账号");

        // 2. 生成验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 3. 存入Redis
        redisTemplate.opsForValue().set("forget_pwd:" + email, code, 5, TimeUnit.MINUTES);

        // 4. 调用短信工具类
        emailUtils.sendVerifyCode(email, code);

        return Result.success("验证码已发送至你的邮箱，请查收");
    }


    /**
     * 验证验证码并重置密码
     */
    @PostMapping("/reset-pwd")
    @Operation(summary = "重置密码（通过QQ邮箱）")
    public Result resetPwd(@RequestBody @Validated ResetPwdByEmailDTO dto) {
        try {
            // 1. 校验验证码
            String code = (String) redisTemplate.opsForValue().get("forget_pwd:" + dto.getEmail());
            if (code == null || !code.equals(dto.getCode())) throw new BusinessException("验证码错误");

            // 2. 重置密码
            userService.resetPwdByEmail(dto);
            return Result.success("密码重置成功，请使用新密码登录");

        } catch (Exception e) {
            log.error("密码重置失败：{}", e.getMessage());
            return Result.error("密码重置失败");
        }
    }

    /**
     * 发送绑定邮箱验证码
     */
    @PostMapping("/send-bind-email-code")
    @Operation(summary = "发送绑定邮箱验证码")
    public Result sendBindEmailCode(@RequestParam String email) {
        // 1. 获取当前登录用户（从登录态中取，如Token/Redis）
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null || claims.get("id") == null) {
            throw new BusinessException("请先登录");
        }
        Integer currentUserId = claims.get("id") instanceof Integer
                ? (Integer) claims.get("id")
                : ((Number) claims.get("id")).intValue();
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 校验邮箱是否已被其他用户绑定
        if (userService.getByEmail(email) != null) {
            throw new BusinessException("该邮箱已被绑定，请更换");
        }

        // 3. 生成6位验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 4. 存入Redis（Key：bind_email:用户ID:邮箱，5分钟过期）
        String redisKey = "bind_email:" + currentUser.getId() + ":" + email;
        redisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);

        // 5. 发送验证码邮件
        emailUtils.sendVerifyCode(email, code);

        return Result.success("验证码已发送至你的邮箱，请查收");
    }

    /**
     * 绑定QQ邮箱（登录后调用）
     */
    @PostMapping("/bind-email")
    @Operation(summary = "绑定QQ邮箱")
    public Result bindEmail(@RequestBody @Validated BindEmailDTO dto) {
        try {
            // 1. 获取当前登录用户
            Map<String, Object> claims = ThreadLocalUtil.get();
            if (claims == null || claims.get("id") == null) {
                throw new BusinessException("请先登录");
            }
            Integer currentUserId = claims.get("id") instanceof Integer
                    ? (Integer) claims.get("id")
                    : ((Number) claims.get("id")).intValue();
            User currentUser = userService.getById(currentUserId);
            if (currentUser == null) {
                throw new BusinessException("用户不存在");
            }

            // 2. 校验验证码
            String redisKey = "bind_email:" + currentUser.getId() + ":" + dto.getEmail();
            String cacheCode = (String) redisTemplate.opsForValue().get(redisKey);
            if (cacheCode == null) {
                throw new BusinessException("验证码已过期，请重新获取");
            }
            if (!cacheCode.equals(dto.getCode())) {
                throw new BusinessException("验证码错误，请核对");
            }

            // 3. 绑定邮箱（更新用户表）
            userService.bindEmail(currentUser.getId(), dto.getEmail());

            // 4. 清除Redis验证码
            redisTemplate.delete(redisKey);

            return Result.success("邮箱绑定成功");
        } catch (BusinessException e) {
            log.error("绑定邮箱失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("绑定邮箱系统异常", e);
            return Result.error("系统异常，请稍后重试");
        }
    }

    @GetMapping("/simple/{id}")
    @Operation(summary = "按ID查询用户简要信息")
    public Result<Map<String, Object>> simple(@PathVariable Integer id) {
        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");
        Map<String, Object> res = new HashMap<>();
        res.put("id", user.getId());
        res.put("userName", user.getUserName());
        res.put("avatar", user.getUserAvatar());
        res.put("role", user.getRole());
        return Result.success(res);
    }

    @GetMapping("/search")
    @Operation(summary = "按姓名搜索用户")
    public Result<List<Map<String, Object>>> search(@RequestParam String keyword) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.like("user_name", keyword).last("limit 20");
        List<Map<String, Object>> data = userService.list(qw).stream()
                .map(u -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", u.getId());
                    item.put("userName", u.getUserName());
                    item.put("avatar", u.getUserAvatar());
                    item.put("role", u.getRole());
                    return item;
                })
                .collect(Collectors.toList());
        return Result.success(data);
    }

    @GetMapping("/profile")
    @Operation(summary = "获取当前用户个人信息")
    public Result<Map<String, Object>> profile() {
        Integer uid = currentUid();
        User user = userService.getById(uid);
        if (user == null) return Result.error("用户不存在");
        Map<String, Object> res = new HashMap<>();
        res.put("id", user.getId());
        res.put("userAccount", user.getUserAccount());
        res.put("userName", user.getUserName());
        res.put("email", user.getEmail());
        res.put("userAvatar", user.getUserAvatar());
        res.put("lastLoginTime", user.getLastLoginTime());
        ClassInfo classInfo = user.getClassId() == null ? null : classInfoService.getById(user.getClassId());
        res.put("className", classInfo == null ? "-" : classInfo.getName());
        res.put("role", user.getRole());
        if (user.getRole() != null && user.getRole() == 1 && user.getClassId() != null) {
            // 优先使用学生表中已关联的辅导员ID；为空时兼容旧逻辑动态匹配
            User counselor = null;
            if (user.getCounselorId() != null) {
                counselor = userService.getById(user.getCounselorId());
            }
            if (counselor == null && classInfo != null && classInfo.getMajorId() != null) {
                counselor = userService.getOne(new QueryWrapper<User>()
                        .eq("identity_type", 2)
                        .apply("find_in_set({0}, charge_major_ids)", classInfo.getMajorId())
                        .last("limit 1"));
            }
            if (counselor == null) {
                counselor = userService.getOne(new QueryWrapper<User>()
                        .eq("identity_type", 2)
                        .apply("find_in_set({0}, charge_class_ids)", user.getClassId())
                        .last("limit 1"));
            }
            res.put("counselorId", counselor == null ? null : counselor.getId());
            res.put("counselorName", counselor == null ? "-" : counselor.getUserName());
        }
        if (user.getRole() != null && user.getRole() == 2) {
            String majors = user.getChargeMajorIds() == null ? "" : user.getChargeMajorIds();
            String managedMajors = "";
            if (StringUtils.isNotBlank(majors)) {
                managedMajors = java.util.Arrays.stream(majors.split(","))
                        .map(String::trim)
                        .filter(StringUtils::isNotBlank)
                        .map(Integer::valueOf)
                        .map(majorInfoService::getById)
                        .filter(java.util.Objects::nonNull)
                        .map(MajorInfo::getName)
                        .collect(Collectors.joining("、"));
            }
            res.put("managedMajors", managedMajors);
            // 兼容旧字段
            String classes = user.getChargeClassIds() == null ? "" : user.getChargeClassIds();
            res.put("managedClasses", classes);
        }
        return Result.success(res);
    }

    @PostMapping("/avatar/upload")
    @Operation(summary = "上传头像")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) return Result.error("请选择文件");
        try {
            String name = "avatar/" + IdFactoryUtil.getFileId() + "_" + (file.getOriginalFilename() != null ? file.getOriginalFilename() : "avatar.jpg");
            String url = AliOssUtils.uploadFile(name, file.getInputStream());
            Map<String, String> res = new HashMap<>();
            res.put("url", url);
            return Result.success(res);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.error("上传失败");
        }
    }

    @PutMapping("/profile")
    @Operation(summary = "更新当前用户头像/邮箱")
    public Result updateProfile(@RequestBody UserProfileUpdateDTO dto) {
        Integer uid = currentUid();
        User update = new User();
        update.setId(uid);
        if (StringUtils.isNotBlank(dto.getEmail())) {
            User sameEmail = userService.getByEmail(dto.getEmail());
            if (sameEmail != null && !sameEmail.getId().equals(uid)) {
                return Result.error("该邮箱已被其他账号绑定");
            }
            update.setEmail(dto.getEmail());
        }
        if (StringUtils.isNotBlank(dto.getUserAvatar())) {
            update.setUserAvatar(dto.getUserAvatar());
        }
        userService.updateById(update);
        return Result.success("更新成功");
    }

    @PutMapping("/password")
    @Operation(summary = "修改当前用户密码")
    public Result updatePassword(@RequestBody @Validated UserPasswordUpdateDTO dto) {
        Integer uid = currentUid();
        User user = userService.getById(uid);
        if (user == null) return Result.error("用户不存在");
        if (!Md5Util.getMd5String(dto.getOldPassword()).equals(user.getPassword())) {
            return Result.error("旧密码错误");
        }
        User update = new User();
        update.setId(uid);
        update.setPassword(md5Util.getMd5String(dto.getNewPassword()));
        userService.updateById(update);
        return Result.success("密码修改成功");
    }

    private Integer currentUid() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null || claims.get("id") == null) throw new BusinessException("请先登录");
        return claims.get("id") instanceof Integer ? (Integer) claims.get("id") : ((Number) claims.get("id")).intValue();
    }
}


