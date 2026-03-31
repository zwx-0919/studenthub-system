package com.it.zwx.studenthub_system.controller.student;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.pojo.dto.extend.ExamPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Exam;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/exam")
@Tag(name = "学生考试管理")
@CrossOrigin
@Slf4j
public class ExamStudentController {

    @Autowired
    private ExamService examService;

    @GetMapping("/page")
    @Operation(summary = "学生查看已启用考试列表（分页）")
    public Result<Page<Exam>> page(@ModelAttribute ExamPageDTO dto) {
        Page<Exam> page = examService.listStudentExams(dto);
        return Result.success(page);
    }
}

