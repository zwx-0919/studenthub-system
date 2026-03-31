package com.it.zwx.studenthub_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.zwx.studenthub_system.exception.BusinessException;
import com.it.zwx.studenthub_system.mapper.LeaveApplyMapper;
import com.it.zwx.studenthub_system.pojo.dto.LeaveApplyDTO;
import com.it.zwx.studenthub_system.pojo.dto.LeaveApproveDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.LeaveApplyPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.LeaveApply;
import com.it.zwx.studenthub_system.pojo.entity.ClassInfo;
import com.it.zwx.studenthub_system.pojo.entity.User;
import com.it.zwx.studenthub_system.service.ClassInfoService;
import com.it.zwx.studenthub_system.service.LeaveApplyService;
import com.it.zwx.studenthub_system.service.MessageService;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.AliOssUtils;
import com.it.zwx.studenthub_system.utils.IdFactoryUtil;
import com.it.zwx.studenthub_system.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeaveApplyServiceImpl extends ServiceImpl<LeaveApplyMapper, LeaveApply> implements LeaveApplyService {

    private final UserService userService;
    private final MessageService messageService;
    private final ClassInfoService classInfoService;

    @Override
    public void submit(LeaveApplyDTO dto) {
        User user = currentUser();
        LeaveApply leave = new LeaveApply();
        leave.setStudentId(user.getId());
        leave.setCounselorId(resolveCounselorIdByClass(user.getClassId()));
        leave.setClassNo(resolveClassName(user.getClassId()));
        leave.setLeaveType(dto.getLeaveType());
        leave.setStartTime(normalizeMinute(dto.getStartTime()));
        leave.setEndTime(normalizeMinute(dto.getEndTime()));
        leave.setReason(dto.getReason());
        leave.setApproveStatus(0);
        leave.setCreateTime(LocalDateTime.now());
        if (dto.getProofFile() != null && !dto.getProofFile().isEmpty()) {
            try {
                String name = "leave/" + IdFactoryUtil.getFileId() + "_" + dto.getProofFile().getOriginalFilename();
                leave.setProofUrl(AliOssUtils.uploadFile(name, dto.getProofFile().getInputStream()));
            } catch (Exception e) {
                throw new BusinessException("请假材料上传失败");
            }
        }
        if (leave.getCounselorId() == null) {
            // 避免 counselor_id NOT NULL 约束触发 SQL 报错
            throw new BusinessException("未找到对应辅导员，请联系管理员完善辅导员负责专业/班级配置");
        }
        baseMapper.insert(leave);
        if (leave.getCounselorId() != null) {
            messageService.pushSystem(leave.getCounselorId(), "有新的请假申请待审批", leave.getId());
        }
    }

    @Override
    public void approve(Integer id, LeaveApproveDTO dto) {
        User counselor = currentUser();
        LeaveApply leave = baseMapper.selectById(id);
        if (leave == null) throw new BusinessException("请假记录不存在");
        if (!counselor.getId().equals(leave.getCounselorId())) throw new BusinessException("无审批权限");
        if (leave.getApproveStatus() != 0) throw new BusinessException("该请假已处理");
        leave.setApproveStatus(dto.getApproveStatus());
        leave.setApproveRemark(dto.getApproveRemark());
        leave.setApproveTime(LocalDateTime.now());
        baseMapper.updateById(leave);
        String statusText = dto.getApproveStatus() != null && dto.getApproveStatus() == 1 ? "通过" : "驳回";
        messageService.pushSystem(leave.getStudentId(), "你的请假申请已" + statusText, leave.getId());
    }

    @Override
    public Page<LeaveApply> studentPage(LeaveApplyPageDTO dto) {
        User user = currentUser();
        Page<LeaveApply> page = new Page<>(dto.getCurrent(), dto.getSize());
        QueryWrapper<LeaveApply> qw = new QueryWrapper<>();
        qw.eq("student_id", user.getId())
                .eq(dto.getApproveStatus() != null, "approve_status", dto.getApproveStatus())
                .orderByDesc("create_time");
        baseMapper.selectPage(page, qw);
        return page;
    }

    @Override
    public Page<LeaveApply> counselorPage(LeaveApplyPageDTO dto) {
        User counselor = currentUser();
        Page<LeaveApply> page = new Page<>(dto.getCurrent(), dto.getSize());
        QueryWrapper<LeaveApply> qw = new QueryWrapper<>();
        qw.eq("counselor_id", counselor.getId())
                .eq(dto.getApproveStatus() != null, "approve_status", dto.getApproveStatus())
                .eq(dto.getClassNo() != null, "class_no", dto.getClassNo())
                .ge(dto.getStartDate() != null, "start_time", dto.getStartDate())
                .le(dto.getEndDate() != null, "end_time", dto.getEndDate())
                .orderByDesc("create_time");
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

    private User currentUser() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null || claims.get("id") == null) throw new BusinessException("用户未登录");
        Integer uid = claims.get("id") instanceof Integer ? (Integer) claims.get("id") : ((Number) claims.get("id")).intValue();
        User user = userService.getById(uid);
        if (user == null) throw new BusinessException("用户不存在");
        return user;
    }

    private LocalDateTime normalizeMinute(LocalDateTime value) {
        return value == null ? null : value.withSecond(0).withNano(0);
    }

    private String resolveClassName(Integer classId) {
        if (classId == null) return null;
        ClassInfo classInfo = classInfoService.getById(classId);
        return classInfo == null ? null : classInfo.getName();
    }

    private Integer resolveCounselorIdByClass(Integer classId) {
        if (classId == null) return null;

        // 优先按“专业”匹配辅导员负责范围：class_info.major_id -> user.charge_major_ids
        ClassInfo classInfo = classInfoService.getById(classId);
        if (classInfo != null && classInfo.getMajorId() != null) {
            User byMajor = userService.getOne(new QueryWrapper<User>()
                    .eq("identity_type", 2)
                    .apply("find_in_set({0}, charge_major_ids)", classInfo.getMajorId())
                    .last("limit 1"));
            if (byMajor != null) return byMajor.getId();
        }

        // 兼容旧逻辑：按班级ID在 charge_class_ids 中匹配
        User byClass = userService.getOne(new QueryWrapper<User>()
                .eq("identity_type", 2)
                .apply("find_in_set({0}, charge_class_ids)", classId)
                .last("limit 1"));
        return byClass == null ? null : byClass.getId();
    }
}

