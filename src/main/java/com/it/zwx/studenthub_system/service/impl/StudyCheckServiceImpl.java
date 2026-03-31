package com.it.zwx.studenthub_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.zwx.studenthub_system.exception.BusinessException;
import com.it.zwx.studenthub_system.mapper.StudyCheckMapper;
import com.it.zwx.studenthub_system.service.ClassInfoService;
import com.it.zwx.studenthub_system.pojo.dto.StudyCheckAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.StudyCheckPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.ClassInfo;
import com.it.zwx.studenthub_system.pojo.entity.StudyCheck;
import com.it.zwx.studenthub_system.pojo.entity.User;
import com.it.zwx.studenthub_system.service.StudyCheckService;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.AliOssUtils;
import com.it.zwx.studenthub_system.utils.IdFactoryUtil;
import com.it.zwx.studenthub_system.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyCheckServiceImpl extends ServiceImpl<StudyCheckMapper, StudyCheck> implements StudyCheckService {

    private final UserService userService;
    private final ClassInfoService classInfoService;

    @Override
    public void addCheck(StudyCheckAddDTO dto) {
        User user = currentUser();
        StudyCheck sc = new StudyCheck();
        sc.setStudentId(user.getId());
        sc.setCounselorId(resolveCounselorIdByClass(user.getClassId()));
        sc.setClassNo(resolveClassName(user.getClassId()));
        sc.setContent(dto.getContent());
        sc.setLocation(dto.getLocation());
        sc.setStatus(1);
        sc.setCheckTime(LocalDateTime.now());
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            try {
                String name = "check/" + IdFactoryUtil.getFileId() + "_" + dto.getImage().getOriginalFilename();
                sc.setImageUrl(AliOssUtils.uploadFile(name, dto.getImage().getInputStream()));
            } catch (Exception e) {
                throw new BusinessException("打卡图片上传失败");
            }
        }
        if (sc.getCounselorId() == null) {
            throw new BusinessException("未找到对应辅导员，请联系管理员完善辅导员负责专业/班级配置");
        }
        baseMapper.insert(sc);
    }

    @Override
    public Page<StudyCheck> studentPage(StudyCheckPageDTO dto) {
        User user = currentUser();
        Page<StudyCheck> page = new Page<>(dto.getCurrent(), dto.getSize());
        QueryWrapper<StudyCheck> qw = new QueryWrapper<>();
        qw.eq("student_id", user.getId())
                .eq(dto.getStatus() != null, "status", dto.getStatus())
                .ge(dto.getStartDate() != null, "check_time", dto.getStartDate())
                .le(dto.getEndDate() != null, "check_time", dto.getEndDate())
                .orderByDesc("check_time");
        baseMapper.selectPage(page, qw);
        return page;
    }

    @Override
    public Page<StudyCheck> counselorPage(StudyCheckPageDTO dto) {
        User counselor = currentUser();
        Page<StudyCheck> page = new Page<>(dto.getCurrent(), dto.getSize());
        QueryWrapper<StudyCheck> qw = new QueryWrapper<>();
        qw.eq("counselor_id", counselor.getId())
                .eq(dto.getStudentId() != null, "student_id", dto.getStudentId())
                .ge(dto.getStartDate() != null, "check_time", dto.getStartDate())
                .le(dto.getEndDate() != null, "check_time", dto.getEndDate())
                .orderByDesc("check_time");
        if (dto.getClassNo() != null && !dto.getClassNo().isBlank()) {
            qw.eq("class_no", dto.getClassNo());
        }
        if (dto.getClassId() != null) {
            qw.inSql("student_id", "select id from user where class_id = " + dto.getClassId());
        }
        baseMapper.selectPage(page, qw);
        page.getRecords().forEach(item -> {
            User stu = userService.getById(item.getStudentId());
            if (stu != null) {
                item.setStudentName(stu.getUserName());
                item.setClassName(resolveClassName(stu.getClassId()));
            }
        });
        return page;
    }

    @Override
    public double classCheckRate(Integer classId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        User counselor = currentUser();
        QueryWrapper<User> userQw = new QueryWrapper<>();
        userQw.eq("identity_type", 1);
        if (classId != null) {
            userQw.eq("class_id", classId);
        } else {
            // 优先按专业负责范围统计，兼容旧 chargeClassIds
            String majors = counselor.getChargeMajorIds() == null ? "" : counselor.getChargeMajorIds();
            if (!majors.isBlank()) {
                userQw.inSql("class_id", "select id from class_info where find_in_set(major_id, '" + majors + "')");
            } else {
                userQw.apply("find_in_set(class_id, {0})", counselor.getChargeClassIds() == null ? "" : counselor.getChargeClassIds());
            }
        }
        List<Integer> studentIds = userService.list(userQw).stream()
                .map(User::getId)
                .collect(Collectors.toList());
        if (studentIds.isEmpty()) return 0D;
        long classTotal = studentIds.size(); // 班级总人数
        QueryWrapper<StudyCheck> checkedQw = new QueryWrapper<>();
        checkedQw.in("student_id", studentIds)
                .ge(startDate != null, "check_time", startDate)
                .le(endDate != null, "check_time", endDate);
        long checkedCount = baseMapper.selectList(checkedQw).stream()
                .map(StudyCheck::getStudentId)
                .distinct()
                .count(); // 已打卡人数（去重）
        return classTotal == 0 ? 0D : checkedCount * 1.0 / classTotal;
    }

    @Override
    public java.util.List<ClassInfo> listClasses() {
        User counselor = currentUser();
        String majors = counselor.getChargeMajorIds() == null ? "" : counselor.getChargeMajorIds();
        if (!majors.isBlank()) {
            return classInfoService.list(new QueryWrapper<ClassInfo>()
                    .inSql("major_id", "select id from major_info where find_in_set(id, '" + majors + "')")
                    .orderByAsc("id"));
        }
        if (counselor.getChargeClassIds() == null || counselor.getChargeClassIds().isBlank()) {
            return java.util.Collections.emptyList();
        }
        return classInfoService.listByIds(java.util.Arrays.stream(counselor.getChargeClassIds().split(","))
                .filter(s -> !s.isBlank())
                .map(Integer::valueOf)
                .collect(Collectors.toList()));
    }

    private String resolveClassName(Integer classId) {
        if (classId == null) return null;
        ClassInfo classInfo = classInfoService.getById(classId);
        return classInfo == null ? null : classInfo.getName();
    }

    private Integer resolveCounselorIdByClass(Integer classId) {
        if (classId == null) return null;
        ClassInfo classInfo = classInfoService.getById(classId);
        if (classInfo != null && classInfo.getMajorId() != null) {
            User byMajor = userService.getOne(new QueryWrapper<User>()
                    .eq("identity_type", 2)
                    .apply("find_in_set({0}, charge_major_ids)", classInfo.getMajorId())
                    .last("limit 1"));
            if (byMajor != null) return byMajor.getId();
        }
        User byClass = userService.getOne(new QueryWrapper<User>()
                .eq("identity_type", 2)
                .apply("find_in_set({0}, charge_class_ids)", classId)
                .last("limit 1"));
        return byClass == null ? null : byClass.getId();
    }

    private User currentUser() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null || claims.get("id") == null) throw new BusinessException("用户未登录");
        Integer uid = claims.get("id") instanceof Integer ? (Integer) claims.get("id") : ((Number) claims.get("id")).intValue();
        User user = userService.getById(uid);
        if (user == null) throw new BusinessException("用户不存在");
        return user;
    }
}

