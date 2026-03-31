package com.it.zwx.studenthub_system.controller.counselor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.pojo.dto.LeaveApproveDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.LeaveApplyPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.LeaveApply;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.service.LeaveApplyService;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/counselor/leave")
@Tag(name = "辅导员请假审批")
@RequiredArgsConstructor
public class LeaveApplyCounselorController {

    private final LeaveApplyService leaveApplyService;
    private final UserService userService;

    @GetMapping("/page")
    @Operation(summary = "辅导员查看本班请假列表")
    public Result<Page<LeaveApply>> page(@ModelAttribute LeaveApplyPageDTO dto) {
        AuthUtil.requireCounselor(AuthUtil.currentUser(userService));
        return Result.success(leaveApplyService.counselorPage(dto));
    }

    @PostMapping("/approve/{id}")
    @Operation(summary = "辅导员审批请假")
    public Result approve(@PathVariable Integer id, @RequestBody @Validated LeaveApproveDTO dto) {
        AuthUtil.requireCounselor(AuthUtil.currentUser(userService));
        leaveApplyService.approve(id, dto);
        return Result.success("审批成功");
    }
}

