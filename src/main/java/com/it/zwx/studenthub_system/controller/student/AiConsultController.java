package com.it.zwx.studenthub_system.controller.student;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.pojo.dto.AiConsultAskDTO;
import com.it.zwx.studenthub_system.pojo.dto.base.PageDTO;
import com.it.zwx.studenthub_system.pojo.entity.AiConsult;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.service.AiConsultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/ai")
@Tag(name = "学生AI咨询")
@RequiredArgsConstructor
public class AiConsultController {

    private final AiConsultService aiConsultService;

    @PostMapping("/ask")
    @Operation(summary = "学生提交咨询问题")
    public Result<AiConsult> ask(@RequestBody AiConsultAskDTO dto) {
        return Result.success(aiConsultService.ask(dto));
    }

    @GetMapping("/page")
    @Operation(summary = "学生查看本人咨询记录")
    public Result<Page<AiConsult>> page(@ModelAttribute PageDTO dto) {
        return Result.success(aiConsultService.myPage(dto));
    }
}

