package com.it.zwx.studenthub_system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.it.zwx.studenthub_system.pojo.dto.CourseDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.CoursePageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Course;

import java.util.List;

public interface CourseService extends IService<Course> {

    Page<Course> listAdminCourses(CoursePageDTO dto);

    Page<Course> listStudentCourses(CoursePageDTO dto);

    void addCourse(CourseDTO dto);

    void updateCourse(Integer id, CourseDTO dto);

    void updateCourseStatus(Integer id);

    void deleteCourse(List<Integer> ids);
}

