package com.it.zwx.studenthub_system.controller.counselor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.pojo.dto.extend.StudyCheckPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.pojo.entity.StudyCheck;
import com.it.zwx.studenthub_system.service.StudyCheckService;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/counselor/check")
@Tag(name = "辅导员打卡")
@RequiredArgsConstructor
public class CounselorStudyCheckController {

    private final StudyCheckService studyCheckService;
    private final UserService userService;

    @GetMapping("/page")
    @Operation(summary = "辅导员查看本班打卡列表")
    public Result<Page<StudyCheck>> page(@ModelAttribute StudyCheckPageDTO dto) {
        AuthUtil.requireCounselor(AuthUtil.currentUser(userService));
        return Result.success(studyCheckService.counselorPage(dto));
    }

    @GetMapping("/rate")
    @Operation(summary = "辅导员查看本班打卡率")
    public Result<Map<String, Object>> rate(@ModelAttribute StudyCheckPageDTO dto) {
        AuthUtil.requireCounselor(AuthUtil.currentUser(userService));
        Map<String, Object> res = new HashMap<>();
        res.put("checkRate", studyCheckService.classCheckRate(dto.getClassId(), dto.getStartDate(), dto.getEndDate()));
        return Result.success(res);
    }

    @GetMapping("/classes")
    @Operation(summary = "辅导员负责的班级列表")
    public Result<java.util.List<com.it.zwx.studenthub_system.pojo.entity.ClassInfo>> classes() {
        AuthUtil.requireCounselor(AuthUtil.currentUser(userService));
        return Result.success(studyCheckService.listClasses());
    }

    @GetMapping("/warnings")
    @Operation(summary = "本周未达标学生预警名单")
    public Result<List<Map<String, Object>>> warnings() {
        AuthUtil.requireCounselor(AuthUtil.currentUser(userService));
        return Result.success(studyCheckService.counselorWarningStudents());
    }

    @GetMapping("/overview")
    @Operation(summary = "辅导员端打卡概览（近7日趋势 + 打卡率）")
    public Result<Map<String, Object>> overview(@ModelAttribute StudyCheckPageDTO dto) {
        AuthUtil.requireCounselor(AuthUtil.currentUser(userService));
        Map<String, Object> res = new HashMap<>();

        // 近7日趋势：用 counselorPage 的 counselor_id 范围（Service 内部已限制 counselor_id）
        Map<String, Object> last7 = new HashMap<>();
        List<String> labels = new java.util.ArrayList<>();
        List<Integer> checkCounts = new java.util.ArrayList<>();
        List<Integer> durationMinutes = new java.util.ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            labels.add(day.toString());
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.atTime(LocalTime.MAX);

            StudyCheckPageDTO d = new StudyCheckPageDTO();
            d.setCurrent(1);
            d.setSize(200);
            d.setClassId(dto.getClassId());
            d.setStartDate(start);
            d.setEndDate(end);
            // counselorPage 会按 counselor_id 过滤，并补齐 studentName/className
            List<StudyCheck> records = studyCheckService.counselorPage(d).getRecords();
            checkCounts.add(records == null ? 0 : records.size());
            int sum = 0;
            if (records != null) {
                for (StudyCheck sc : records) {
                    sum += sc.getStudyDurationMinutes() == null ? 0 : sc.getStudyDurationMinutes();
                }
            }
            durationMinutes.add(sum);
        }
        last7.put("labels", labels);
        last7.put("checkCounts", checkCounts);
        last7.put("durationMinutes", durationMinutes);
        res.put("last7Days", last7);

        res.put("checkRate", studyCheckService.classCheckRate(dto.getClassId(), dto.getStartDate(), dto.getEndDate()));
        return Result.success(res);
    }
}

