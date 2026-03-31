package com.it.zwx.studenthub_system.controller.student;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.pojo.dto.LeaveApplyDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.LeaveApplyPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.LeaveApply;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.service.LeaveApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/leave")
@Tag(name = "学生请假")
@RequiredArgsConstructor
public class LeaveApplyStudentController {

    private final LeaveApplyService leaveApplyService;

    @PostMapping("/add")
    @Operation(summary = "学生提交请假申请")
    public Result add(@Validated @ModelAttribute LeaveApplyDTO dto) {
        leaveApplyService.submit(dto);
        return Result.success("提交成功");
    }

    @GetMapping("/page")
    @Operation(summary = "学生查看本人请假记录")
    public Result<Page<LeaveApply>> page(@ModelAttribute LeaveApplyPageDTO dto) {
        return Result.success(leaveApplyService.studentPage(dto));
    }
}

