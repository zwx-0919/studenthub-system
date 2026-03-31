package com.it.zwx.studenthub_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.it.zwx.studenthub_system.mapper.CourseMapper;
import com.it.zwx.studenthub_system.mapper.ExamMapper;
import com.it.zwx.studenthub_system.pojo.entity.Course;
import com.it.zwx.studenthub_system.pojo.entity.Exam;
import com.it.zwx.studenthub_system.pojo.entity.ClassInfo;
import com.it.zwx.studenthub_system.pojo.entity.User;
import com.it.zwx.studenthub_system.service.ClassInfoService;
import com.it.zwx.studenthub_system.service.MessageService;
import com.it.zwx.studenthub_system.service.ReminderService;
import com.it.zwx.studenthub_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    private final UserService userService;
    private final ClassInfoService classInfoService;
    private final CourseMapper courseMapper;
    private final ExamMapper examMapper;
    private final MessageService messageService;

    @Override
    public void pushLoginReminders(Integer userId) {
        Map<String, List<String>> upcoming = getUpcomingReminderText(userId);
        List<String> reminders = new ArrayList<>();
        reminders.addAll(upcoming.getOrDefault("courses", Collections.emptyList()));
        reminders.addAll(upcoming.getOrDefault("exams", Collections.emptyList()));
        for (String text : reminders) {
            messageService.pushSystem(userId, text, null);
        }
    }

    @Override
    public Map<String, List<String>> getUpcomingReminderText(Integer userId) {
        User user = userService.getById(userId);
        String classNo = resolveClassName(user == null ? null : user.getClassId());
        if (user == null || classNo == null || classNo.isBlank()) {
            return Map.of("courses", Collections.emptyList(), "exams", Collections.emptyList());
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime courseEnd = now.plusHours(24);
        LocalDateTime examEnd = now.plusDays(7);

        QueryWrapper<Course> courseQw = new QueryWrapper<>();
        courseQw.eq("class_no", classNo)
                .eq("status", 1)
                .ge("time", now)
                .le("time", courseEnd)
                .orderByAsc("time");
        List<Course> courses = courseMapper.selectList(courseQw);

        QueryWrapper<Exam> examQw = new QueryWrapper<>();
        examQw.eq("class_no", classNo)
                .eq("status", 1)
                .ge("time", now)
                .le("time", examEnd)
                .orderByAsc("time");
        List<Exam> exams = examMapper.selectList(examQw);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M月d日 HH:mm");
        List<String> courseTexts = new ArrayList<>();
        for (Course c : courses) {
            courseTexts.add(String.format("【课程提醒】您有《%s》课程将于%s在%s开始",
                    safe(c.getCourseName()), c.getTime() == null ? "" : c.getTime().format(dtf), safe(c.getLocation())));
        }

        List<String> examTexts = new ArrayList<>();
        for (Exam e : exams) {
            long days = e.getTime() == null ? 0 : Math.max(0, Duration.between(now, e.getTime()).toDays());
            examTexts.add(String.format("【考试提醒】您有《%s》考试将于%s后%s在%s进行",
                    safe(e.getExamName()), days + "天", e.getTime() == null ? "" : e.getTime().format(dtf), safe(e.getLocation())));
        }

        Map<String, List<String>> result = new HashMap<>();
        result.put("courses", courseTexts);
        result.put("exams", examTexts);
        return result;
    }

    private String resolveClassName(Integer classId) {
        if (classId == null) return null;
        ClassInfo classInfo = classInfoService.getById(classId);
        return classInfo == null ? null : classInfo.getName();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}

