package com.it.zwx.studenthub_system.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.constant.MessageConstant;
import com.it.zwx.studenthub_system.pojo.dto.CourseDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.CoursePageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Course;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/course")
@Tag(name = "管理员课程管理")
@CrossOrigin
@Slf4j
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping("/page")
    @Operation(summary = "课程分页查询（管理员）")
    public Result<Page<Course>> page(@ModelAttribute CoursePageDTO dto) {
        Page<Course> page = courseService.listAdminCourses(dto);
        return Result.success(page);
    }

    @PostMapping("/add")
    @Operation(summary = "管理员新增课程")
    public Result add(@RequestBody @Validated CourseDTO dto) {
        try {
            courseService.addCourse(dto);
            return Result.success(MessageConstant.ADD_SUCCESS);
        } catch (Exception e) {
            log.error("新增课程失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "管理员修改课程")
    public Result update(@PathVariable Integer id, @RequestBody @Validated CourseDTO dto) {
        try {
            courseService.updateCourse(id, dto);
            return Result.success(MessageConstant.UPDATE_SUCCESS);
        } catch (Exception e) {
            log.error("修改课程失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    @Operation(summary = "管理员切换课程启用/禁用状态")
    public Result toggleStatus(@PathVariable Integer id) {
        try {
            courseService.updateCourseStatus(id);
            return Result.success(MessageConstant.UPDATE_SUCCESS);
        } catch (Exception e) {
            log.error("切换课程状态失败", e);
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping
    @Operation(summary = "管理员批量删除课程")
    public Result delete(@RequestBody List<Integer> ids) {
        try {
            courseService.deleteCourse(ids);
            return Result.success(MessageConstant.DELETE_SUCCESS);
        } catch (Exception e) {
            log.error("删除课程失败", e);
            return Result.error(e.getMessage());
        }
    }
}

