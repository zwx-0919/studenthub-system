package com.it.zwx.studenthub_system.controller.admin;

import com.it.zwx.studenthub_system.pojo.entity.ClassInfo;
import com.it.zwx.studenthub_system.pojo.entity.MajorInfo;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.service.ClassInfoService;
import com.it.zwx.studenthub_system.service.MajorInfoService;
import com.it.zwx.studenthub_system.utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/base")
@Tag(name = "后台基础数据")
@RequiredArgsConstructor
public class BaseDataController {
    private final ClassInfoService classInfoService;
    private final MajorInfoService majorInfoService;
    private final UserService userService;

    @GetMapping("/classes")
    @Operation(summary = "班级列表")
    public Result<List<ClassInfo>> classes() {
        AuthUtil.requireAdmin(AuthUtil.currentUser(userService));
        return Result.success(classInfoService.list());
    }

    @GetMapping("/majors")
    @Operation(summary = "专业列表")
    public Result<List<MajorInfo>> majors() {
        AuthUtil.requireAdmin(AuthUtil.currentUser(userService));
        return Result.success(majorInfoService.list());
    }
}
