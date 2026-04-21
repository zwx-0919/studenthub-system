package com.it.zwx.studenthub_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.it.zwx.studenthub_system.constant.MessageConstant;
import com.it.zwx.studenthub_system.exception.BusinessException;
import com.it.zwx.studenthub_system.mapper.CourseMapper;
import com.it.zwx.studenthub_system.pojo.dto.CourseDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.CoursePageDTO;
import com.it.zwx.studenthub_system.pojo.entity.ClassInfo;
import com.it.zwx.studenthub_system.pojo.entity.Course;
import com.it.zwx.studenthub_system.pojo.entity.User;
import com.it.zwx.studenthub_system.service.ClassInfoService;
import com.it.zwx.studenthub_system.service.CourseService;
import com.it.zwx.studenthub_system.service.RedisCacheService;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.it.zwx.studenthub_system.constant.StatusConstant.DISABLE;
import static com.it.zwx.studenthub_system.constant.StatusConstant.ENABLE;

@Service
@Transactional
@Slf4j
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    @Autowired
    private UserService userService;
    @Autowired
    private ClassInfoService classInfoService;
    @Autowired
    private RedisCacheService redisCacheService;

    private static final String COURSE_CACHE_PATTERN = "cache:course:*";
    private static final String COURSE_ADMIN_PAGE_PREFIX = "cache:course:admin:page:";
    private static final String COURSE_STUDENT_PAGE_PREFIX = "cache:course:student:page:";

    private void evictCourseCaches() {
        redisCacheService.deleteCacheByPattern(COURSE_CACHE_PATTERN);
    }

    @Override
    public Page<Course> listAdminCourses(CoursePageDTO dto) {
        long current = dto.getCurrent() == null || dto.getCurrent() < 1 ? 1 : dto.getCurrent();
        long size = dto.getSize() == null || dto.getSize() < 1 ? 10 : dto.getSize();
        String cacheKey = COURSE_ADMIN_PAGE_PREFIX
                + "c=" + current
                + "&s=" + size
                + "&course=" + (dto.getCourseNameKeyword() == null ? "" : dto.getCourseNameKeyword())
                + "&teacher=" + (dto.getTeacherNameKeyword() == null ? "" : dto.getTeacherNameKeyword())
                + "&status=" + (dto.getStatus() == null ? "" : dto.getStatus())
                + "&term=" + (dto.getTerm() == null ? "" : dto.getTerm())
                + "&classNo=" + (dto.getClassNo() == null ? "" : dto.getClassNo())
                + "&majorId=" + (dto.getMajorId() == null ? "" : dto.getMajorId())
                + "&start=" + (dto.getTimeStart() == null ? "" : dto.getTimeStart())
                + "&end=" + (dto.getTimeEnd() == null ? "" : dto.getTimeEnd());
        Page<Course> cached = redisCacheService.getCache(cacheKey, new TypeReference<Page<Course>>() {});
        if (cached != null) {
            return cached;
        }

        Page<Course> page = new Page<>(dto.getCurrent(), dto.getSize());

        QueryWrapper<Course> query = new QueryWrapper<>();
        query.like(dto.getCourseNameKeyword() != null, "course_name", dto.getCourseNameKeyword())
                .like(dto.getTeacherNameKeyword() != null, "teacher_name", dto.getTeacherNameKeyword())
                .eq(dto.getStatus() != null, "status", dto.getStatus())
                .eq(dto.getTerm() != null, "term", dto.getTerm())
                .eq(dto.getClassNo() != null, "class_no", dto.getClassNo())
                .eq(dto.getMajorId() != null, "major_id", dto.getMajorId())
                .ge(dto.getTimeStart() != null, "time", dto.getTimeStart())
                .le(dto.getTimeEnd() != null, "time", dto.getTimeEnd());

        baseMapper.selectPage(page, query);
        redisCacheService.setCache(cacheKey, page, 5);
        return page;
    }

    @Override
    public Page<Course> listStudentCourses(CoursePageDTO dto) {
        User currentUser = currentUser();
        // 学生只看启用状态
        long current = dto.getCurrent() == null || dto.getCurrent() < 1 ? 1 : dto.getCurrent();
        long size = dto.getSize() == null || dto.getSize() < 1 ? 10 : dto.getSize();
        Page<Course> page = new Page<>(dto.getCurrent(), dto.getSize());
        Integer studentMajorId = getStudentMajorId(currentUser);

        String cacheKey = COURSE_STUDENT_PAGE_PREFIX
                + "uid=" + (currentUser.getId() == null ? "" : currentUser.getId())
                + "&majorId=" + (studentMajorId == null ? "" : studentMajorId)
                + "&classId=" + (currentUser.getClassId() == null ? "" : currentUser.getClassId())
                + "&c=" + current
                + "&s=" + size
                + "&course=" + (dto.getCourseNameKeyword() == null ? "" : dto.getCourseNameKeyword())
                + "&teacher=" + (dto.getTeacherNameKeyword() == null ? "" : dto.getTeacherNameKeyword())
                + "&term=" + (dto.getTerm() == null ? "" : dto.getTerm())
                + "&start=" + (dto.getTimeStart() == null ? "" : dto.getTimeStart())
                + "&end=" + (dto.getTimeEnd() == null ? "" : dto.getTimeEnd());
        Page<Course> cached = redisCacheService.getCache(cacheKey, new TypeReference<Page<Course>>() {});
        if (cached != null) {
            return cached;
        }

        QueryWrapper<Course> query = new QueryWrapper<>();
        query.like(dto.getCourseNameKeyword() != null, "course_name", dto.getCourseNameKeyword())
                .like(dto.getTeacherNameKeyword() != null, "teacher_name", dto.getTeacherNameKeyword())
                .eq("status", ENABLE)
                .eq(dto.getTerm() != null, "term", dto.getTerm())
                // 学生端按 major_id 匹配；若学生 major 不可用则回退 class_no 兼容历史数据
                .eq(studentMajorId != null, "major_id", studentMajorId)
                .eq(studentMajorId == null && currentUser.getClassId() != null, "class_no", resolveClassName(currentUser.getClassId()))
                .ge(dto.getTimeStart() != null, "time", dto.getTimeStart())
                .le(dto.getTimeEnd() != null, "time", dto.getTimeEnd());

        baseMapper.selectPage(page, query);
        redisCacheService.setCache(cacheKey, page, 5);
        return page;
    }

    private Integer getStudentMajorId(User currentUser) {
        if (currentUser == null || currentUser.getClassId() == null) return null;
        ClassInfo classInfo = classInfoService.getById(currentUser.getClassId());
        return classInfo == null ? null : classInfo.getMajorId();
    }

    private String resolveClassName(Integer classId) {
        if (classId == null) return null;
        ClassInfo classInfo = classInfoService.getById(classId);
        return classInfo == null ? null : classInfo.getName();
    }

    @Override
    public void addCourse(CourseDTO dto) {
        validateClassMajorRelation(dto.getClassNo(), dto.getMajorId());
        Course course = new Course();
        course.setCourseName(dto.getCourseName());
        course.setCourseNumber(dto.getCourseNumber());
        course.setTeacherName(dto.getTeacherName());
        course.setTime(dto.getTime());
        course.setLocation(dto.getLocation());
        course.setTerm(dto.getTerm());
        course.setClassNo(dto.getClassNo());
        course.setMajorId(dto.getMajorId());
        course.setStatus(dto.getStatus() == null ? ENABLE : dto.getStatus());
        course.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(course);
        evictCourseCaches();
    }

    @Override
    public void updateCourse(Integer id, CourseDTO dto) {
        Course exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("课程不存在");
        }
        validateClassMajorRelation(dto.getClassNo(), dto.getMajorId());

        exist.setCourseName(dto.getCourseName());
        exist.setCourseNumber(dto.getCourseNumber());
        exist.setTeacherName(dto.getTeacherName());
        exist.setTime(dto.getTime());
        exist.setLocation(dto.getLocation());
        exist.setTerm(dto.getTerm());
        exist.setClassNo(dto.getClassNo());
        exist.setMajorId(dto.getMajorId());
        if (dto.getStatus() != null) {
            exist.setStatus(dto.getStatus());
        }
        exist.setUpdateTime(LocalDateTime.now());

        baseMapper.updateById(exist);
        evictCourseCaches();
    }

    @Override
    public void updateCourseStatus(Integer id) {
        UpdateWrapper<Course> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .in("status", DISABLE, ENABLE)
                .setSql("status = CASE WHEN status=0 THEN 1 ELSE 0 END");

        int rows = baseMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("课程不存在，或当前状态不支持切换");
        }
        evictCourseCaches();
    }

    @Override
    public void deleteCourse(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的课程");
        }

        int rows = baseMapper.deleteBatchIds(ids);
        if (rows == 0) {
            throw new BusinessException(MessageConstant.OPERATE_FAILED);
        }
        evictCourseCaches();
    }

    private User currentUser() {
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

    private void validateClassMajorRelation(String classNo, Integer majorId) {
        if (classNo == null || classNo.isBlank() || majorId == null) {
            return;
        }
        ClassInfo classInfo = classInfoService.lambdaQuery()
                .eq(ClassInfo::getName, classNo)
                .one();
        if (classInfo == null) {
            throw new BusinessException("班级不存在，请先维护班级基础数据");
        }
        if (!majorId.equals(classInfo.getMajorId())) {
            throw new BusinessException("班级与专业不匹配，请重新选择");
        }
    }

}

