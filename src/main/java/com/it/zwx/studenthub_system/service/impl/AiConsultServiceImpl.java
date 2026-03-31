package com.it.zwx.studenthub_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.zwx.studenthub_system.exception.BusinessException;
import com.it.zwx.studenthub_system.mapper.AiConsultMapper;
import com.it.zwx.studenthub_system.pojo.dto.AiConsultAskDTO;
import com.it.zwx.studenthub_system.pojo.dto.base.PageDTO;
import com.it.zwx.studenthub_system.pojo.entity.AiConsult;
import com.it.zwx.studenthub_system.pojo.entity.ClassInfo;
import com.it.zwx.studenthub_system.pojo.entity.Course;
import com.it.zwx.studenthub_system.pojo.entity.Exam;
import com.it.zwx.studenthub_system.pojo.entity.User;
import com.it.zwx.studenthub_system.service.AiConsultService;
import com.it.zwx.studenthub_system.service.ClassInfoService;
import com.it.zwx.studenthub_system.service.CourseService;
import com.it.zwx.studenthub_system.service.ExamService;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.ThreadLocalUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiConsultServiceImpl extends ServiceImpl<AiConsultMapper, AiConsult> implements AiConsultService {

    private final UserService userService;
    private final CourseService courseService;
    private final ExamService examService;
    private final ClassInfoService classInfoService;
    private final ChatClient chatClient;

    public AiConsultServiceImpl(ChatClient.Builder chatClientBuilder,
                                UserService userService,
                                CourseService courseService,
                                ExamService examService,
                                ClassInfoService classInfoService) {
        this.userService = userService;
        this.courseService = courseService;
        this.examService = examService;
        this.classInfoService = classInfoService;
        this.chatClient = chatClientBuilder.defaultSystem(
                        """
                                你是StudentHub校园AI助手。请用中文、友好且准确地回答学生问题。
                                你会收到学生姓名、班级、课程安排、考试安排等上下文。
                                回答必须严格围绕：课程、考试、时间、地点。
                                如果上下文中没有信息，请明确说明“当前系统暂无相关安排”。
                                回答控制在3句话内，优先直接给时间和地点，不要编造。
                                今天日期是 {current_date}。
                                """
                )
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @Override
    public AiConsult ask(AiConsultAskDTO dto) {
        User user = currentUser();
        AiConsult consult = new AiConsult();
        consult.setStudentId(user.getId());
        consult.setQuestion(dto.getQuestion());

        String answer = callAi(user, dto.getQuestion());
        consult.setAnswer(answer);
        consult.setCreateTime(LocalDateTime.now());
        baseMapper.insert(consult);
        return consult;
    }

    @Override
    public Page<AiConsult> myPage(PageDTO dto) {
        User user = currentUser();
        Page<AiConsult> page = new Page<>(dto.getCurrent(), dto.getSize());
        QueryWrapper<AiConsult> qw = new QueryWrapper<>();
        qw.eq("student_id", user.getId()).orderByDesc("create_time");
        baseMapper.selectPage(page, qw);
        return page;
    }

    private String callAi(User user, String question) {
        if (question == null || question.trim().isEmpty()) {
            return "你好 " + safeName(user) + "，请告诉我你要查询的课程或考试信息。";
        }
        String studentName = safeName(user);
        String context = buildScheduleContext(user);
        String reply;
        try {
            reply = chatClient.prompt()
                    .user(u -> u.text("""
                            学生姓名：{studentName}
                            学生问题：{question}

                            以下是系统内已知课程考试安排，请优先基于该信息回答：
                            {scheduleContext}
                            """)
                            .param("studentName", studentName)
                            .param("question", question)
                            .param("scheduleContext", context)
                    )
                    .system(s -> s.param("current_date", LocalDate.now().toString()))
                    .call()
                    .content();
        } catch (Exception ex) {
            reply = fallbackAnswer(question, context);
        }

        String normalized = (reply == null || reply.isBlank())
                ? "当前系统暂无相关安排，你可以稍后再试。"
                : reply.trim();
        if (normalized.startsWith("你好 " + studentName)) {
            return normalized;
        }
        return "你好 " + studentName + "，" + normalized;
    }

    private String buildScheduleContext(User user) {
        Integer majorId = null;
        if (user.getClassId() != null) {
            ClassInfo classInfo = classInfoService.getById(user.getClassId());
            majorId = classInfo == null ? null : classInfo.getMajorId();
        }

        List<Course> courses = courseService.lambdaQuery()
                .eq(Course::getStatus, 1)
                .eq(majorId != null, Course::getMajorId, majorId)
                .last("limit 10")
                .list();
        List<Exam> exams = examService.lambdaQuery()
                .eq(Exam::getStatus, 1)
                .eq(majorId != null, Exam::getMajorId, majorId)
                .last("limit 30")
                .list();

        String courseText = courses.isEmpty() ? "无" : courses.stream()
                .map(c -> String.format("课程：%s，时间：%s，地点：%s",
                        nvl(c.getCourseName()), nvl(c.getTime()), nvl(c.getLocation())))
                .collect(Collectors.joining("；"));
        String examText = exams.isEmpty() ? "无" : exams.stream()
                .map(e -> String.format("考试：%s，时间：%s，地点：%s",
                        nvl(e.getExamName()), nvl(e.getTime()), nvl(e.getLocation())))
                .collect(Collectors.joining("；"));

        return "课程安排：" + courseText + "\n考试安排：" + examText;
    }

    private String fallbackAnswer(String question, String context) {
        if (question == null || question.isBlank()) {
            return "请告诉我你要查询的课程或考试信息。";
        }
        if (question.contains("考试")) {
            return context.contains("考试安排：无") ? "当前系统暂无考试安排。" : "我已为你查询考试安排，请重点关注时间和地点。";
        }
        if (question.contains("课程") || question.contains("上课")) {
            return context.contains("课程安排：无") ? "当前系统暂无课程安排。" : "我已为你查询课程安排，请重点关注上课时间和地点。";
        }
        if (question.contains("时间") || question.contains("几点")) {
            return "你可以告诉我是课程还是考试，我会给你对应的时间信息。";
        }
        if (question.contains("地点") || question.contains("教室")) {
            return "你可以告诉我是课程还是考试，我会给你对应的地点信息。";
        }
        return "我可以帮你查询课程、考试、时间和地点信息。";
    }

    private String nvl(Object val) {
        return val == null ? "未安排" : String.valueOf(val);
    }

    private String safeName(User user) {
        return user.getUserName() == null || user.getUserName().isBlank() ? "同学" : user.getUserName();
    }

    private User currentUser() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null || claims.get("id") == null) throw new BusinessException("用户未登录");
        Integer uid = claims.get("id") instanceof Integer ? (Integer) claims.get("id") : ((Number) claims.get("id")).intValue();
        User user = userService.getById(uid);
        if (user == null) throw new BusinessException("用户不存在");
        return user;
    }
}
