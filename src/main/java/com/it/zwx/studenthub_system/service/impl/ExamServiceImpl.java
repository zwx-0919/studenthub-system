package com.it.zwx.studenthub_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.zwx.studenthub_system.constant.MessageConstant;
import com.it.zwx.studenthub_system.constant.StatusConstant;
import com.it.zwx.studenthub_system.exception.BusinessException;
import com.it.zwx.studenthub_system.mapper.ExamMapper;
import com.it.zwx.studenthub_system.pojo.dto.ExamDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.ExamPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.ClassInfo;
import com.it.zwx.studenthub_system.pojo.entity.Exam;
import com.it.zwx.studenthub_system.pojo.entity.User;
import com.it.zwx.studenthub_system.service.ClassInfoService;
import com.it.zwx.studenthub_system.service.ExamService;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements ExamService {
    @Autowired
    private UserService userService;
    @Autowired
    private ClassInfoService classInfoService;

    @Override
    public Page<Exam> listAdminExams(ExamPageDTO dto) {
        Page<Exam> page = new Page<>(dto.getCurrent(), dto.getSize());

        QueryWrapper<Exam> query = new QueryWrapper<>();
        query.eq(dto.getStatus() != null, "status", dto.getStatus())
                .eq(dto.getCourseId() != null, "course_id", dto.getCourseId())
                .eq(dto.getClassNo() != null, "class_no", dto.getClassNo())
                .eq(dto.getMajorId() != null, "major_id", dto.getMajorId())
                .like(dto.getExamNameKeyword() != null, "exam_name", dto.getExamNameKeyword())
                .ge(dto.getTimeStart() != null, "time", dto.getTimeStart())
                .le(dto.getTimeEnd() != null, "time", dto.getTimeEnd())
                .ge(dto.getRemindTimeStart() != null, "remind_time", dto.getRemindTimeStart())
                .le(dto.getRemindTimeEnd() != null, "remind_time", dto.getRemindTimeEnd());

        baseMapper.selectPage(page, query);
        return page;
    }

    @Override
    public Page<Exam> listStudentExams(ExamPageDTO dto) {
        User currentUser = currentUser();
        // 学生只看启用状态
        Page<Exam> page = new Page<>(dto.getCurrent(), dto.getSize());
        Integer studentMajorId = getStudentMajorId(currentUser);

        QueryWrapper<Exam> query = new QueryWrapper<>();
        query.eq("status", StatusConstant.ENABLE)
                .eq(dto.getCourseId() != null, "course_id", dto.getCourseId())
                // 学生端按 major_id 匹配；若学生 major 不可用则回退 class_no 兼容历史数据
                .eq(studentMajorId != null, "major_id", studentMajorId)
                .eq(studentMajorId == null && currentUser.getClassId() != null, "class_no", resolveClassName(currentUser.getClassId()))
                .like(dto.getExamNameKeyword() != null, "exam_name", dto.getExamNameKeyword())
                .ge(dto.getTimeStart() != null, "time", dto.getTimeStart())
                .le(dto.getTimeEnd() != null, "time", dto.getTimeEnd())
                .ge(dto.getRemindTimeStart() != null, "remind_time", dto.getRemindTimeStart())
                .le(dto.getRemindTimeEnd() != null, "remind_time", dto.getRemindTimeEnd());

        baseMapper.selectPage(page, query);
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
    public void addExam(ExamDTO dto) {
        validateClassMajorRelation(dto.getClassNo(), dto.getMajorId());
        Exam exam = new Exam();
        exam.setCourseId(dto.getCourseId());
        exam.setExamName(dto.getExamName());
        exam.setTime(dto.getTime());
        exam.setLocation(dto.getLocation());
        exam.setRemindTime(dto.getRemindTime());
        exam.setClassNo(dto.getClassNo());
        exam.setMajorId(dto.getMajorId());
        exam.setStatus(dto.getStatus() == null ? StatusConstant.ENABLE : dto.getStatus());
        exam.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(exam);
    }

    @Override
    public void updateExam(Integer id, ExamDTO dto) {
        Exam exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("考试不存在");
        }
        validateClassMajorRelation(dto.getClassNo(), dto.getMajorId());

        exist.setCourseId(dto.getCourseId());
        exist.setExamName(dto.getExamName());
        exist.setTime(dto.getTime());
        exist.setLocation(dto.getLocation());
        exist.setRemindTime(dto.getRemindTime());
        exist.setClassNo(dto.getClassNo());
        exist.setMajorId(dto.getMajorId());
        if (dto.getStatus() != null) {
            exist.setStatus(dto.getStatus());
        }
        exist.setUpdateTime(LocalDateTime.now());

        baseMapper.updateById(exist);
    }

    @Override
    public void updateExamStatus(Integer id) {
        UpdateWrapper<Exam> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .in("status", StatusConstant.DISABLE, StatusConstant.ENABLE)
                .setSql("status = CASE WHEN status=0 THEN 1 ELSE 0 END");

        int rows = baseMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("考试不存在，或当前状态不支持切换");
        }
    }

    @Override
    public void deleteExam(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的考试");
        }

        int rows = baseMapper.deleteBatchIds(ids);
        if (rows == 0) {
            throw new BusinessException(MessageConstant.OPERATE_FAILED);
        }
    }

    private User currentUser() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null || claims.get("id") == null) throw new BusinessException("用户未登录");
        Integer uid = claims.get("id") instanceof Integer ? (Integer) claims.get("id") : ((Number) claims.get("id")).intValue();
        User user = userService.getById(uid);
        if (user == null) throw new BusinessException("用户不存在");
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

