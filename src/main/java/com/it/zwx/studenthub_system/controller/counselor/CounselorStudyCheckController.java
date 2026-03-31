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

import java.util.HashMap;
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
}

