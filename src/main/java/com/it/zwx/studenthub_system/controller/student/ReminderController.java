package com.it.zwx.studenthub_system.controller.student;

import com.it.zwx.studenthub_system.exception.BusinessException;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.service.ReminderService;
import com.it.zwx.studenthub_system.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/student/remind")
@Tag(name = "学生提醒")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping("/upcoming")
    @Operation(summary = "学生拉取近期课程/考试提醒")
    public Result<Map<String, java.util.List<String>>> upcoming() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null || claims.get("id") == null) {
            throw new BusinessException("用户未登录");
        }
        Integer uid = claims.get("id") instanceof Integer ? (Integer) claims.get("id") : ((Number) claims.get("id")).intValue();
        return Result.success(reminderService.getUpcomingReminderText(uid));
    }
}

