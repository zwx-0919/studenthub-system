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
import com.it.zwx.studenthub_system.pojo.entity.LeaveApply;
import com.it.zwx.studenthub_system.pojo.entity.Notice;
import com.it.zwx.studenthub_system.pojo.entity.User;
import com.it.zwx.studenthub_system.service.AiConsultService;
import com.it.zwx.studenthub_system.service.ClassInfoService;
import com.it.zwx.studenthub_system.service.CourseService;
import com.it.zwx.studenthub_system.service.ExamService;
import com.it.zwx.studenthub_system.service.LeaveApplyService;
import com.it.zwx.studenthub_system.service.NoticeService;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.ThreadLocalUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiConsultServiceImpl extends ServiceImpl<AiConsultMapper, AiConsult> implements AiConsultService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserService userService;
    private final CourseService courseService;
    private final ExamService examService;
    private final ClassInfoService classInfoService;
    private final NoticeService noticeService;
    private final LeaveApplyService leaveApplyService;
    private final ChatClient chatClient;

    public AiConsultServiceImpl(ChatClient.Builder chatClientBuilder,
                                UserService userService,
                                CourseService courseService,
                                ExamService examService,
                                ClassInfoService classInfoService,
                                NoticeService noticeService,
                                LeaveApplyService leaveApplyService) {
        this.userService = userService;
        this.courseService = courseService;
        this.examService = examService;
        this.classInfoService = classInfoService;
        this.noticeService = noticeService;
        this.leaveApplyService = leaveApplyService;
        this.chatClient = chatClientBuilder.defaultSystem(
                        """
                                你是StudentHub校园AI助手。请用中文、友好且准确地回答学生问题。
                                你会收到学生姓名、班级、课程安排、考试安排、公告、请假记录等上下文。
                                回答必须严格围绕校园业务：课程、考试、时间、地点、公告、请假、流程、状态。
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
        String sessionId = normalizeSessionId(dto.getSessionId());
        String question = dto.getQuestion();
        String answer = callAi(user, sessionId, question);

        AiConsult consult = new AiConsult();
        consult.setStudentId(user.getId());
        consult.setQuestion(question);
        consult.setAnswer(answer);
        consult.setCreateTime(LocalDateTime.now());
        consult.setSessionId(sessionId);
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

    @Override
    public boolean save(AiConsult entity) {
        if (entity != null && entity.getSessionId() != null) {
            entity.setSessionId(null);
        }
        return super.save(entity);
    }

    private String callAi(User user, String sessionId, String question) {
        if (question == null || question.trim().isEmpty()) {
            return greeting(user) + "请告诉我你要查询的校园信息。";
        }

        String studentName = safeName(user);
        String leaveAnswer = handleLeaveQuery(user, question);
        if (leaveAnswer != null) {
            return greeting(user) + leaveAnswer;
        }

        String context = buildCampusContext(user, question);
        String memory = buildConversationMemory(user.getId(), question);
        String reply;
        try {
            reply = chatClient.prompt()
                    .user(u -> u.text("""
                            学生姓名：{studentName}
                            当前问题：{question}

                            最近对话记录：
                            {memory}

                            以下是系统内已知校园数据，请优先基于该信息回答：
                            {context}
                            """)
                            .param("studentName", studentName)
                            .param("question", question)
                            .param("memory", memory)
                            .param("context", context)
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
        return greeting(user) + normalized;
    }

    private String buildCampusContext(User user, String question) {
        Integer majorId = resolveMajorId(user);
        DateRange range = parseTimeRange(question);

        List<Course> courses = courseService.lambdaQuery()
                .eq(Course::getStatus, 1)
                .eq(majorId != null, Course::getMajorId, majorId)
                .ge(range != null, Course::getTime, range.start)
                .lt(range != null, Course::getTime, range.end)
                .orderByAsc(Course::getTime)
                .last("limit 20")
                .list();
        List<Exam> exams = examService.lambdaQuery()
                .eq(Exam::getStatus, 1)
                .eq(majorId != null, Exam::getMajorId, majorId)
                .ge(range != null, Exam::getTime, range.start)
                .lt(range != null, Exam::getTime, range.end)
                .orderByAsc(Exam::getTime)
                .last("limit 20")
                .list();
        List<Notice> notices = noticeService.lambdaQuery()
                .eq(Notice::getStatus, 1)
                .ge(range != null, Notice::getPublishTime, range.start)
                .lt(range != null, Notice::getPublishTime, range.end)
                .orderByDesc(Notice::getPublishTime)
                .last("limit 8")
                .list();
        List<LeaveApply> leaves = leaveApplyService.lambdaQuery()
                .eq(LeaveApply::getStudentId, user.getId())
                .ge(range != null, LeaveApply::getCreateTime, range.start)
                .lt(range != null, LeaveApply::getCreateTime, range.end)
                .orderByDesc(LeaveApply::getCreateTime)
                .last("limit 8")
                .list();

        List<String> sections = new ArrayList<>();
        sections.add("时间范围：" + describeRange(range));
        sections.add("课程安排：" + summarizeCourses(courses));
        sections.add("考试安排：" + summarizeExams(exams));
        sections.add("公告通知：" + summarizeNotices(notices));
        sections.add("请假汇总：" + summarizeLeaveSummary(leaves));
        sections.add("请假明细：" + summarizeLeaves(leaves));
        return String.join("\n", sections);
    }

    private String summarizeCourses(List<Course> courses) {
        if (courses.isEmpty()) return "无";
        return courses.stream()
                .limit(8)
                .map(c -> String.format("%s | 老师：%s | 时间：%s | 地点：%s | 学期：%s",
                        nvl(c.getCourseName()), nvl(c.getTeacherName()),
                        formatTime(c.getTime()), nvl(c.getLocation()), nvl(c.getTerm())))
                .collect(Collectors.joining("；"));
    }

    private String summarizeExams(List<Exam> exams) {
        if (exams.isEmpty()) return "无";
        return exams.stream()
                .limit(8)
                .map(e -> String.format("%s | 时间：%s | 地点：%s",
                        nvl(e.getExamName()), formatTime(e.getTime()), nvl(e.getLocation())))
                .collect(Collectors.joining("；"));
    }

    private String summarizeNotices(List<Notice> notices) {
        if (notices.isEmpty()) return "无";
        return notices.stream()
                .limit(5)
                .map(n -> String.format("%s | 发布时间：%s",
                        nvl(n.getTitle()), formatTime(n.getPublishTime())))
                .collect(Collectors.joining("；"));
    }

    private String summarizeLeaveSummary(List<LeaveApply> leaves) {
        if (leaves.isEmpty()) return "无";
        long total = leaves.size();
        long pending = leaves.stream().filter(l -> Objects.equals(l.getApproveStatus(), 0)).count();
        long approved = leaves.stream().filter(l -> Objects.equals(l.getApproveStatus(), 1)).count();
        long rejected = leaves.stream().filter(l -> Objects.equals(l.getApproveStatus(), 2)).count();
        return String.format("共 %d 条，待审批 %d 条，已通过 %d 条，已驳回 %d 条", total, pending, approved, rejected);
    }

    private String summarizeLeaves(List<LeaveApply> leaves) {
        if (leaves.isEmpty()) return "无";
        return leaves.stream()
                .limit(5)
                .map(l -> String.format("%s请假 | %s - %s | 状态：%s",
                        leaveTypeText(l.getLeaveType()), formatTime(l.getStartTime()),
                        formatTime(l.getEndTime()), leaveStatusText(l.getApproveStatus())))
                .collect(Collectors.joining("；"));
    }

    private String buildConversationMemory(Integer studentId, String currentQuestion) {
        List<AiConsult> history = lambdaQuery()
                .eq(AiConsult::getStudentId, studentId)
                .orderByDesc(AiConsult::getCreateTime)
                .last("limit 8")
                .list();
        if (history.isEmpty()) {
            return "无";
        }

        List<AiConsult> relevant = history.stream()
                .sorted((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()))
                .filter(item -> item != null && item.getQuestion() != null)
                .filter(item -> isRelevant(currentQuestion, item.getQuestion()))
                .collect(Collectors.toList());

        List<AiConsult> source = relevant.isEmpty() ? history : relevant;
        return source.stream()
                .sorted((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()))
                .map(item -> "用户：" + nvl(item.getQuestion()) + "\n助手：" + nvl(item.getAnswer()))
                .collect(Collectors.joining("\n---\n"));
    }

    private String fallbackAnswer(String question, String context) {
        String lower = question == null ? "" : question;
        if (lower.isBlank()) {
            return "请告诉我你要查询的校园信息。";
        }
        if (containsAny(lower, "请假", "销假", "审批", "假条", "休假")) {
            return context.contains("请假记录：无") ? "当前系统暂无请假记录。" : "我已为你查询请假记录和当前状态。";
        }
        if (containsAny(lower, "考试", "考哪", "考场", "补考")) {
            return context.contains("考试安排：无") ? "当前系统暂无考试安排。" : "我已为你查询考试安排，请重点关注时间和地点。";
        }
        if (containsAny(lower, "课程", "上课", "课表", "老师")) {
            return context.contains("课程安排：无") ? "当前系统暂无课程安排。" : "我已为你查询课程安排，请重点关注上课时间和地点。";
        }
        if (containsAny(lower, "公告", "通知", "消息")) {
            return context.contains("公告通知：无") ? "当前系统暂无公告通知。" : "我已为你查询最近公告通知。";
        }
        if (containsAny(lower, "时间", "几点", "什么时候", "日期")) {
            return "你可以告诉我是课程、考试、公告还是请假事项，我会帮你查对应时间。";
        }
        if (containsAny(lower, "地点", "教室", "哪里")) {
            return "你可以告诉我是课程还是考试，我会给你对应的地点信息。";
        }
        return "我可以帮你查询课程、考试、公告、请假、时间和地点信息。";
    }

    private String handleLeaveQuery(User user, String question) {
        if (!containsAny(question, "请假", "销假", "审批", "假条", "休假")) {
            return null;
        }
        List<LeaveApply> leaves = leaveApplyService.lambdaQuery()
                .eq(LeaveApply::getStudentId, user.getId())
                .orderByDesc(LeaveApply::getCreateTime)
                .last("limit 20")
                .list();
        if (leaves.isEmpty()) {
            return "当前系统暂无请假记录。";
        }

        DateRange range = parseTimeRange(question);
        List<LeaveApply> filtered = leaves.stream()
                .filter(l -> range == null || (l.getCreateTime() != null && !l.getCreateTime().isBefore(range.start) && l.getCreateTime().isBefore(range.end)))
                .collect(Collectors.toList());
        List<LeaveApply> source = filtered.isEmpty() ? leaves : filtered;

        long total = source.size();
        long pending = source.stream().filter(l -> Objects.equals(l.getApproveStatus(), 0)).count();
        long approved = source.stream().filter(l -> Objects.equals(l.getApproveStatus(), 1)).count();
        long rejected = source.stream().filter(l -> Objects.equals(l.getApproveStatus(), 2)).count();

        StringBuilder sb = new StringBuilder();
        sb.append("当前").append(range == null ? "最近" : describeRange(range)).append("请假共 ").append(total)
                .append(" 条，待审批 ").append(pending)
                .append(" 条，已通过 ").append(approved)
                .append(" 条，已驳回 ").append(rejected).append(" 条。");

        String details = source.stream()
                .limit(5)
                .map(l -> String.format("%s请假 | %s - %s | 状态：%s",
                        leaveTypeText(l.getLeaveType()), formatTime(l.getStartTime()),
                        formatTime(l.getEndTime()), leaveStatusText(l.getApproveStatus())))
                .collect(Collectors.joining("；"));
        if (!details.isBlank()) {
            sb.append(" 明细：").append(details).append("。");
        }
        return sb.toString();
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null) return false;
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    private boolean isRelevant(String currentQuestion, String historyQuestion) {
        if (currentQuestion == null || currentQuestion.isBlank() || historyQuestion == null || historyQuestion.isBlank()) {
            return false;
        }
        String cq = currentQuestion.trim();
        String hq = historyQuestion.trim();

        if (containsAny(cq, "考试", "考场", "补考") && containsAny(hq, "考试", "考场", "补考")) return true;
        if (containsAny(cq, "课程", "上课", "课表", "老师") && containsAny(hq, "课程", "上课", "课表", "老师")) return true;
        if (containsAny(cq, "公告", "通知", "消息") && containsAny(hq, "公告", "通知", "消息")) return true;
        if (containsAny(cq, "请假", "销假", "审批", "假条") && containsAny(hq, "请假", "销假", "审批", "假条")) return true;
        if (containsAny(cq, "时间", "几点", "什么时候", "日期") && containsAny(hq, "时间", "几点", "什么时候", "日期")) return true;
        if (containsAny(cq, "地点", "教室", "哪里") && containsAny(hq, "地点", "教室", "哪里")) return true;
        return false;
    }

    private boolean matchQuestion(String question, String... fields) {
        if (question == null || question.isBlank()) {
            return true;
        }
        String normalized = question.trim();
        for (String field : fields) {
            if (field != null && !field.isBlank() && normalized.contains(field)) {
                return true;
            }
        }
        return false;
    }

    private String greeting(User user) {
        return "你好 " + safeName(user) + "，";
    }

    private Integer resolveMajorId(User user) {
        if (user.getClassId() == null) {
            return null;
        }
        ClassInfo classInfo = classInfoService.getById(user.getClassId());
        return classInfo == null ? null : classInfo.getMajorId();
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "未安排" : time.format(TIME_FORMATTER);
    }

    private String describeRange(DateRange range) {
        if (range == null) return "不限";
        return formatTime(range.start) + " ~ " + formatTime(range.end);
    }

    private DateRange parseTimeRange(String question) {
        LocalDate today = LocalDate.now();
        if (question == null || question.isBlank()) {
            return null;
        }
        String q = question.trim();
        if (containsAny(q, "今天", "今日", "当天")) return DateRange.of(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        if (containsAny(q, "明天")) {
            LocalDate d = today.plusDays(1);
            return DateRange.of(d.atStartOfDay(), d.plusDays(1).atStartOfDay());
        }
        if (containsAny(q, "后天")) {
            LocalDate d = today.plusDays(2);
            return DateRange.of(d.atStartOfDay(), d.plusDays(1).atStartOfDay());
        }
        if (containsAny(q, "本周", "这周", "周内")) {
            LocalDate start = today.with(DayOfWeek.MONDAY);
            LocalDate end = start.plusDays(7);
            return DateRange.of(start.atStartOfDay(), end.atStartOfDay());
        }
        if (containsAny(q, "下周")) {
            LocalDate start = today.with(DayOfWeek.MONDAY).plusWeeks(1);
            LocalDate end = start.plusDays(7);
            return DateRange.of(start.atStartOfDay(), end.atStartOfDay());
        }
        if (containsAny(q, "本月", "这个月")) {
            LocalDate start = today.withDayOfMonth(1);
            LocalDate end = start.plusMonths(1);
            return DateRange.of(start.atStartOfDay(), end.atStartOfDay());
        }
        return null;
    }

    private boolean isLeaveRelevant(String question, LeaveApply leave) {
        if (question == null || question.isBlank()) return true;
        String q = question.trim();
        String typeText = leaveTypeText(leave.getLeaveType());
        String statusText = leaveStatusText(leave.getApproveStatus());
        String reason = leave.getReason() == null ? "" : leave.getReason();
        return containsAny(q, typeText, statusText) || reason.contains(q);
    }

    private String normalizeSessionId(String sessionId) {
        return (sessionId == null || sessionId.isBlank()) ? UUID.randomUUID().toString().replace("-", "") : sessionId.trim();
    }

    private static class DateRange {
        private final LocalDateTime start;
        private final LocalDateTime end;

        private DateRange(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }

        static DateRange of(LocalDateTime start, LocalDateTime end) {
            return new DateRange(start, end);
        }
    }

    private String leaveTypeText(Integer leaveType) {
        if (leaveType == null) return "未知";
        return switch (leaveType) {
            case 1 -> "事假";
            case 2 -> "病假";
            default -> "其他";
        };
    }

    private String leaveStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待审批";
            case 1 -> "已通过";
            case 2 -> "已驳回";
            default -> "未知";
        };
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
