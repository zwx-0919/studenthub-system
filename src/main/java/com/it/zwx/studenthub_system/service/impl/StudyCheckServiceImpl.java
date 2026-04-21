package com.it.zwx.studenthub_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.it.zwx.studenthub_system.exception.BusinessException;
import com.it.zwx.studenthub_system.mapper.StudyCheckMapper;
import com.it.zwx.studenthub_system.service.ClassInfoService;
import com.it.zwx.studenthub_system.pojo.dto.StudyCheckAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.StudyCheckPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.ClassInfo;
import com.it.zwx.studenthub_system.pojo.entity.StudyCheck;
import com.it.zwx.studenthub_system.pojo.entity.User;
import com.it.zwx.studenthub_system.service.RedisCacheService;
import com.it.zwx.studenthub_system.service.StudyCheckService;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.AliOssUtils;
import com.it.zwx.studenthub_system.utils.IdFactoryUtil;
import com.it.zwx.studenthub_system.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyCheckServiceImpl extends ServiceImpl<StudyCheckMapper, StudyCheck> implements StudyCheckService {

    private final UserService userService;
    private final ClassInfoService classInfoService;
    private final RedisCacheService redisCacheService;

    private static final String CHECK_STUDENT_PAGE_PREFIX = "cache:check:student:";
    private static final String CHECK_COUNSELOR_PAGE_PREFIX = "cache:check:counselor:";
    private static final String CHECK_CACHE_PATTERN = "cache:check:*";

    @Value("${studenthub.check.weekly-min-checks:3}")
    private int weeklyMinChecks;
    @Value("${studenthub.check.weekly-min-duration-minutes:180}")
    private int weeklyMinDurationMinutes;
    @Value("${studenthub.check.monthly-min-checks:12}")
    private int monthlyMinChecks;
    @Value("${studenthub.check.monthly-min-duration-minutes:720}")
    private int monthlyMinDurationMinutes;

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
        sc.setStudyDurationMinutes(dto.getStudyDurationMinutes() == null ? 0 : dto.getStudyDurationMinutes());
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

        // 写入后清缓存：学生本人分页 + 对应辅导员分页（按用户维度）
        redisCacheService.deleteCacheByPattern(CHECK_STUDENT_PAGE_PREFIX + user.getId() + ":page:*");
        redisCacheService.deleteCacheByPattern(CHECK_COUNSELOR_PAGE_PREFIX + sc.getCounselorId() + ":page:*");
    }

    @Override
    public Map<String, Object> studentCheckWarning() {
        User user = currentUser();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.minusDays(7);
        LocalDateTime monthStart = now.minus(30, ChronoUnit.DAYS);
        Map<String, Object> res = new HashMap<>();
        res.put("weekly", buildPeriodStats(user.getId(), weekStart, now, weeklyMinChecks, weeklyMinDurationMinutes));
        res.put("monthly", buildPeriodStats(user.getId(), monthStart, now, monthlyMinChecks, monthlyMinDurationMinutes));
        return res;
    }

    private Map<String, Object> buildPeriodStats(int studentId, LocalDateTime start, LocalDateTime end,
                                                 int minChecks, int minDuration) {
        QueryWrapper<StudyCheck> qw = new QueryWrapper<>();
        qw.eq("student_id", studentId).ge("check_time", start).le("check_time", end);
        List<StudyCheck> list = baseMapper.selectList(qw);
        int totalMinutes = list.stream()
                .mapToInt(c -> c.getStudyDurationMinutes() == null ? 0 : c.getStudyDurationMinutes())
                .sum();
        boolean alert = list.size() < minChecks || totalMinutes < minDuration;
        Map<String, Object> m = new HashMap<>();
        m.put("checkCount", list.size());
        m.put("totalDurationMinutes", totalMinutes);
        m.put("minChecks", minChecks);
        m.put("minDurationMinutes", minDuration);
        m.put("needsAlert", alert);
        return m;
    }

    @Override
    public List<Map<String, Object>> counselorWarningStudents() {
        User counselor = currentUser();
        QueryWrapper<User> userQw = new QueryWrapper<>();
        userQw.eq("identity_type", 1);
        String majors = counselor.getChargeMajorIds() == null ? "" : counselor.getChargeMajorIds();
        if (!majors.isBlank()) {
            userQw.inSql("class_id", "select id from class_info where find_in_set(major_id, '" + majors + "')");
        } else if (counselor.getChargeClassIds() != null && !counselor.getChargeClassIds().isBlank()) {
            userQw.apply("find_in_set(class_id, {0})", counselor.getChargeClassIds());
        } else {
            return new ArrayList<>();
        }
        List<User> students = userService.list(userQw);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.minusDays(7);
        List<Map<String, Object>> out = new ArrayList<>();
        for (User stu : students) {
            Map<String, Object> w = buildPeriodStats(stu.getId(), weekStart, now, weeklyMinChecks, weeklyMinDurationMinutes);
            if (Boolean.TRUE.equals(w.get("needsAlert"))) {
                Map<String, Object> row = new HashMap<>();
                row.put("studentId", stu.getId());
                row.put("studentName", stu.getUserName());
                row.put("className", resolveClassName(stu.getClassId()));
                row.put("weeklyCheckCount", w.get("checkCount"));
                row.put("weeklyDurationMinutes", w.get("totalDurationMinutes"));
                out.add(row);
            }
        }
        out.sort((a, b) -> Integer.compare(
                (Integer) b.getOrDefault("weeklyDurationMinutes", 0),
                (Integer) a.getOrDefault("weeklyDurationMinutes", 0)));
        return out;
    }

    @Override
    public Page<StudyCheck> studentPage(StudyCheckPageDTO dto) {
        User user = currentUser();
        long current = dto.getCurrent() == null || dto.getCurrent() < 1 ? 1 : dto.getCurrent();
        long size = dto.getSize() == null || dto.getSize() < 1 ? 10 : dto.getSize();
        String cacheKey = CHECK_STUDENT_PAGE_PREFIX + user.getId()
                + ":page:"
                + "c=" + current
                + "&s=" + size
                + "&status=" + (dto.getStatus() == null ? "" : dto.getStatus())
                + "&start=" + (dto.getStartDate() == null ? "" : dto.getStartDate())
                + "&end=" + (dto.getEndDate() == null ? "" : dto.getEndDate());
        Page<StudyCheck> cached = redisCacheService.getCache(cacheKey, new TypeReference<Page<StudyCheck>>() {});
        if (cached != null) {
            return cached;
        }

        Page<StudyCheck> page = new Page<>(current, size);
        QueryWrapper<StudyCheck> qw = new QueryWrapper<>();
        qw.eq("student_id", user.getId())
                .eq(dto.getStatus() != null, "status", dto.getStatus())
                .ge(dto.getStartDate() != null, "check_time", dto.getStartDate())
                .le(dto.getEndDate() != null, "check_time", dto.getEndDate())
                .orderByDesc("check_time");
        baseMapper.selectPage(page, qw);
        redisCacheService.setCache(cacheKey, page, 5);
        return page;
    }

    @Override
    public Page<StudyCheck> counselorPage(StudyCheckPageDTO dto) {
        User counselor = currentUser();
        long current = dto.getCurrent() == null || dto.getCurrent() < 1 ? 1 : dto.getCurrent();
        long size = dto.getSize() == null || dto.getSize() < 1 ? 10 : dto.getSize();
        String cacheKey = CHECK_COUNSELOR_PAGE_PREFIX + counselor.getId()
                + ":page:"
                + "c=" + current
                + "&s=" + size
                + "&studentId=" + (dto.getStudentId() == null ? "" : dto.getStudentId())
                + "&classNo=" + (dto.getClassNo() == null ? "" : dto.getClassNo())
                + "&classId=" + (dto.getClassId() == null ? "" : dto.getClassId())
                + "&start=" + (dto.getStartDate() == null ? "" : dto.getStartDate())
                + "&end=" + (dto.getEndDate() == null ? "" : dto.getEndDate());
        Page<StudyCheck> cached = redisCacheService.getCache(cacheKey, new TypeReference<Page<StudyCheck>>() {});
        if (cached != null) {
            return cached;
        }

        Page<StudyCheck> page = new Page<>(current, size);
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
        redisCacheService.setCache(cacheKey, page, 5);
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

