package com.it.zwx.studenthub_system.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.constant.MessageConstant;
import com.it.zwx.studenthub_system.pojo.dto.ExamDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.ExamPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Exam;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/exam")
@Tag(name = "管理员考试管理")
@CrossOrigin
@Slf4j
public class ExamController {

    @Autowired
    private ExamService examService;

    @GetMapping("/page")
    @Operation(summary = "考试分页查询（管理员）")
    public Result<Page<Exam>> page(@ModelAttribute ExamPageDTO dto) {
        return Result.success(examService.listAdminExams(dto));
    }

    @PostMapping("/add")
    @Operation(summary = "管理员新增考试")
    public Result add(@RequestBody @Validated ExamDTO dto) {
        try {
            examService.addExam(dto);
            return Result.success(MessageConstant.ADD_SUCCESS);
        } catch (Exception e) {
            log.error("新增考试失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "管理员修改考试")
    public Result update(@PathVariable Integer id, @RequestBody @Validated ExamDTO dto) {
        try {
            examService.updateExam(id, dto);
            return Result.success(MessageConstant.UPDATE_SUCCESS);
        } catch (Exception e) {
            log.error("修改考试失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    @Operation(summary = "管理员切换考试启用/禁用状态")
    public Result toggleStatus(@PathVariable Integer id) {
        try {
            examService.updateExamStatus(id);
            return Result.success(MessageConstant.UPDATE_SUCCESS);
        } catch (Exception e) {
            log.error("切换考试状态失败", e);
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping
    @Operation(summary = "管理员批量删除考试")
    public Result delete(@RequestBody List<Integer> ids) {
        try {
            examService.deleteExam(ids);
            return Result.success(MessageConstant.DELETE_SUCCESS);
        } catch (Exception e) {
            log.error("删除考试失败", e);
            return Result.error(e.getMessage());
        }
    }
}

