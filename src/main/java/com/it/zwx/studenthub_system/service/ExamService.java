package com.it.zwx.studenthub_system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.it.zwx.studenthub_system.pojo.dto.ExamDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.ExamPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Exam;

import java.util.List;

public interface ExamService extends IService<Exam> {

    Page<Exam> listAdminExams(ExamPageDTO dto);

    Page<Exam> listStudentExams(ExamPageDTO dto);

    void addExam(ExamDTO dto);

    void updateExam(Integer id, ExamDTO dto);

    void updateExamStatus(Integer id);

    void deleteExam(List<Integer> ids);
}

