package com.it.zwx.studenthub_system.controller.student;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.pojo.dto.extend.CoursePageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Course;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.service.CourseService;
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
@RequestMapping("/api/student/course")
@Tag(name = "学生课程管理")
@CrossOrigin
@Slf4j
public class CourseStudentController {

    @Autowired
    private CourseService courseService;

    @GetMapping("/page")
    @Operation(summary = "学生查看已发布课程列表（分页）")
    public Result<Page<Course>> page(@ModelAttribute CoursePageDTO dto) {
        Page<Course> page = courseService.listStudentCourses(dto);
        return Result.success(page);
    }
}

