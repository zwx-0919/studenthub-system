package com.it.zwx.studenthub_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.zwx.studenthub_system.constant.MessageConstant;
import com.it.zwx.studenthub_system.constant.RoleConstant;
import com.it.zwx.studenthub_system.exception.BusinessException;
import com.it.zwx.studenthub_system.exception.GlobalException;
import com.it.zwx.studenthub_system.mapper.UserMapper;
import com.it.zwx.studenthub_system.pojo.dto.LoginDTO;
import com.it.zwx.studenthub_system.pojo.dto.ResetPwdByEmailDTO;
import com.it.zwx.studenthub_system.pojo.dto.UserAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.UserPageDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.UserStatusDTO;
import com.it.zwx.studenthub_system.pojo.entity.ClassInfo;
import com.it.zwx.studenthub_system.pojo.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.it.zwx.studenthub_system.service.ClassInfoService;
import com.it.zwx.studenthub_system.service.RedisCacheService;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.JwtUtil;
import com.it.zwx.studenthub_system.utils.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private Md5Util md5Util;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ClassInfoService classInfoService;
    @Autowired
    private RedisCacheService redisCacheService;

    private static final String USER_CACHE_PREFIX = "cache:user:info:";

    private void evictUserCache(Integer id) {
        if (id != null) {
            redisCacheService.deleteCache(USER_CACHE_PREFIX + id);
        }
    }

    @Override
    public User getById(Serializable id) {
        if (id == null) {
            return null;
        }
        Integer uid = id instanceof Integer ? (Integer) id : ((Number) id).intValue();
        String key = USER_CACHE_PREFIX + uid;
        try {
            User cached = redisCacheService.getCache(key, new TypeReference<User>() {});
            if (cached != null) {
                return cached;
            }
        } catch (Exception ignored) {
        }
        User user = super.getById(id);
        if (user != null) {
            redisCacheService.setCache(key, user, 30);
        }
        return user;
    }

    @Override
    public boolean updateById(User entity) {
        boolean ok = super.updateById(entity);
        if (ok && entity != null && entity.getId() != null) {
            evictUserCache(entity.getId());
        }
        return ok;
    }
    /**
     * 根据用户名查询用户
     *
     * @param userAccount
     * @return
     */
    @Override
    public User getByUserName(String userAccount) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("user_account", userAccount);

        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 登录
     *
     * @param loginDTO
     * @return
     */
    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {

        String userAccount = loginDTO.getUserAccount();
        String password = loginDTO.getPassword();
        Integer role = loginDTO.getRole();

        User user = getByUserName(userAccount);

        System.out.println(user);
        if (user == null) {
            throw new BusinessException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 3. 校验角色
        Integer identityType = user.getIdentityType() == null ? user.getRole() : user.getIdentityType();
        if (!identityType.equals(role)) {
            throw new BusinessException(MessageConstant.ROLE_NOT_MATCH);
        }
        // 3. 校验密码是否正确
        if (!Md5Util.getMd5String(password).equals(user.getPassword())) {
            throw new BusinessException(MessageConstant.PASSWORD_ERROR);
        }

        // 4. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);

        // 5. 生成Token
        Map<String, Object> tokenParams = Map.of(
                "user_account", user.getUserAccount(),
                "id", user.getId()
        );
        return new HashMap<>() {{
            String token = JwtUtil.genToken(tokenParams);
            redisTemplate.opsForValue().set("login:" + user.getId() + ":" + token, "1", 12, TimeUnit.HOURS);
            put("token", token);
            put("user", user);
        }};
    }

    /**
     * 查看用户列表
     *
     * @return
     */
    @Override
    public Page<User> listUser(UserPageDTO userPageDTO) {
        Integer current = userPageDTO.getCurrent();
        Integer size = userPageDTO.getSize();
        Page<User> page = new Page<>(current, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Integer role = userPageDTO.getRole();
        queryWrapper.like(StringUtils.isNotBlank(userPageDTO.getUserName()), "user_name", userPageDTO.getUserName())
                .like(StringUtils.isNotBlank(userPageDTO.getUserAccount()), "user_account", userPageDTO.getUserAccount())
                .eq(userPageDTO.getStatus() != null, "status", userPageDTO.getStatus())
                .eq(userPageDTO.getClassId() != null, "class_id", userPageDTO.getClassId())
                // 兼容历史数据：优先 identity_type，缺失时回退 role 字段
                .and(role == null, w -> w
                        .eq("identity_type", RoleConstant.STUDENT)
                        .or().eq("identity_type", RoleConstant.TEACHER)
                        .or(iw -> iw.isNull("identity_type").eq("role", RoleConstant.STUDENT))
                        .or(iw -> iw.isNull("identity_type").eq("role", RoleConstant.TEACHER)))
                .and(role != null, w -> w
                        .eq("identity_type", role)
                        .or(iw -> iw.isNull("identity_type").eq("role", role)));

        baseMapper.selectPage(page, queryWrapper);
        return page;
    }

    /**
     * 更新用户状态
     *
     * @param statusDTO
     */
    @Override
    public void updateUserStatus(UserStatusDTO statusDTO) {

        if (statusDTO.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (statusDTO.getStatus() == null) {
            throw new BusinessException("当前状态值不能为空");
        }

        if (!Arrays.asList(0, 1).contains(statusDTO.getStatus())) {
            throw new BusinessException("状态值只能是0或1");
        }

        Integer newStatus = statusDTO.getStatus() == 1 ? 0 : 1;

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", statusDTO.getId())
                .set("status", newStatus);

        int rows = baseMapper.update(null, updateWrapper);

        if (rows == 0) {
            throw new BusinessException(MessageConstant.OPERATE_FAILED);
        }
    }

    /**
     * 批量删除用户
     *
     * @param ids
     */
    @Override
    public void deleteUser(List<Integer> ids) {
        if (ids != null) {
            for (Integer id : ids) {
                evictUserCache(id);
            }
        }
        baseMapper.deleteByIds(ids);
    }

    /**
     * 根据邮箱查询用户
     *
     * @param email
     * @return
     */
    @Override
    public User getByEmail(String email) {
        // 添加参数验证，避免空指针异常
        if (StringUtils.isBlank(email)) {
            return null;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 重置密码
     *
     * @param dto
     */
    @Override
    public void resetPwdByEmail(ResetPwdByEmailDTO dto) {
        // 校验两次密码是否一致（
        if (!dto.getNewPwd().equals(dto.getConfirmPwd())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 2. 校验用户是否存在（兜底，防止DTO校验被绕过）
        User user = getByEmail(dto.getEmail());
        if (user == null) {
            throw new BusinessException("该邮箱未绑定任何账号");
        }

        // 3. 加密新密码（核心）
        String encryptPwd = md5Util.getMd5String(dto.getNewPwd());

        // 4. 更新数据库
        user.setPassword(encryptPwd);
        int rows = baseMapper.updateById(user);

        if (rows == 0) {
            throw new BusinessException("密码重置失败，请重试");
        }
        // 5. 清除Redis验证码（防止重复使用）
        redisTemplate.delete("forget_pwd:" + dto.getEmail());

    }

    /**
     * 绑定邮箱
     *
     * @param id
     * @param email
     */
    @Override
    public void bindEmail(Integer id, String email) {
        // 1. 再次校验邮箱唯一性（兜底）
        if (getByEmail(email) != null) {
            throw new BusinessException("该邮箱已被绑定");
        }

        // 2. 更新用户邮箱字段
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        int rows = baseMapper.updateById(user);

        if (rows == 0) {
            throw new BusinessException("邮箱绑定失败，请重试");
        }
    }

    @Override
    public void addUser(UserAddDTO dto) {
        if (dto.getUserAccount() == null || dto.getUserAccount().isBlank()) {
            throw new BusinessException("账号不能为空");
        }
        if (getByUserName(dto.getUserAccount()) != null) {
            throw new BusinessException("账号已存在");
        }
        User user = new User();
        user.setUserAccount(dto.getUserAccount());
        user.setUserName(dto.getUserName());
        user.setPassword(Md5Util.getMd5String(dto.getPassword() == null ? "123456" : dto.getPassword()));
        user.setIdentityType(dto.getIdentityType());
        user.setRole(dto.getIdentityType());
        user.setClassId(dto.getClassId());
        if (RoleConstant.STUDENT == dto.getIdentityType() && dto.getClassId() != null) {
            user.setCounselorId(resolveCounselorIdByClass(dto.getClassId()));
        }
        // 兼容：旧版用 chargeClassIds，新版用 chargeMajorIds
        user.setChargeClassIds(dto.getChargeClassIds());
        user.setChargeMajorIds(dto.getChargeMajorIds());
        user.setEmail(dto.getEmail());
        user.setUserAvatar(dto.getUserAvatar());
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        user.setCreateTime(LocalDateTime.now());
        baseMapper.insert(user);
    }

    private Integer resolveCounselorIdByClass(Integer classId) {
        ClassInfo classInfo = classInfoService.getById(classId);
        if (classInfo != null && classInfo.getMajorId() != null) {
            User counselorByMajor = baseMapper.selectOne(new QueryWrapper<User>()
                    .eq("identity_type", RoleConstant.TEACHER)
                    .apply("find_in_set({0}, charge_major_ids)", classInfo.getMajorId())
                    .last("limit 1"));
            if (counselorByMajor != null) {
                return counselorByMajor.getId();
            }
        }
        User counselorByClass = baseMapper.selectOne(new QueryWrapper<User>()
                .eq("identity_type", RoleConstant.TEACHER)
                .apply("find_in_set({0}, charge_class_ids)", classId)
                .last("limit 1"));
        return counselorByClass == null ? null : counselorByClass.getId();
    }
}

