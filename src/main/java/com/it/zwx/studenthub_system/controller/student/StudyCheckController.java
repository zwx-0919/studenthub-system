package com.it.zwx.studenthub_system.controller.student;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.pojo.dto.StudyCheckAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.StudyCheckPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.pojo.entity.StudyCheck;
import com.it.zwx.studenthub_system.service.StudyCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/check")
@Tag(name = "学生打卡")
@RequiredArgsConstructor
public class StudyCheckController {

    private final StudyCheckService studyCheckService;

    @PostMapping("/add")
    @Operation(summary = "学生打卡提交（上传图片+定位）")
    public Result add(@Validated @ModelAttribute StudyCheckAddDTO dto) {
        studyCheckService.addCheck(dto);
        return Result.success("打卡成功");
    }

    @GetMapping("/page")
    @Operation(summary = "学生查看本人打卡记录")
    public Result<Page<StudyCheck>> page(@ModelAttribute StudyCheckPageDTO dto) {
        return Result.success(studyCheckService.studentPage(dto));
    }
}

