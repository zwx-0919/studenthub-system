<template>
  <div class="schedule-page">
    <!-- 课程卡片 -->
    <div class="schedule-card course-card">
      <div class="card-header">
        <div class="header-icon">📚</div>
        <div class="header-content">
          <h3>我的课程</h3>
          <p>查看本学期所修课程</p>
        </div>
        <button class="refresh-btn" @click="loadCourses" title="刷新课程列表">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 4 23 10 17 10"></polyline>
            <polyline points="1 20 1 14 7 14"></polyline>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
          </svg>
        </button>
      </div>
      
      <div v-if="loadingCourses" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-else-if="courses.length > 0" class="course-list">
        <div v-for="c in courses" :key="c.id" class="course-item">
          <div class="course-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"></path>
              <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"></path>
            </svg>
          </div>
          <div class="course-info">
            <div class="course-name">{{ c.courseName }}</div>
            <div v-if="c.teacherName" class="course-teacher">👨‍🏫 {{ c.teacherName }}</div>
            <div v-if="c.credit" class="course-credit">🏅 {{ c.credit }} 学分</div>
            <div v-if="c.classTime" class="course-time">⏰ {{ c.classTime }}</div>
            <div v-if="c.classroom" class="course-location">📍 {{ c.classroom }}</div>
          </div>
          <div v-if="c.score" class="course-score" :class="getScoreClass(c.score)">
            {{ c.score }}
          </div>
        </div>
      </div>
      
      <div v-else class="empty-state">
        <div class="empty-icon">📖</div>
        <h4>暂无课程</h4>
        <p>当前学期没有安排课程</p>
      </div>
    </div>

    <!-- 考试卡片 -->
    <div class="schedule-card exam-card">
      <div class="card-header">
        <div class="header-icon">📝</div>
        <div class="header-content">
          <h3>我的考试</h3>
          <p>查看近期考试安排</p>
        </div>
        <button class="refresh-btn" @click="loadExams" title="刷新考试列表">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 4 23 10 17 10"></polyline>
            <polyline points="1 20 1 14 7 14"></polyline>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
          </svg>
        </button>
      </div>
      
      <div v-if="loadingExams" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-else-if="exams.length > 0" class="exam-list">
        <div v-for="e in sortedExams" :key="e.id" class="exam-item" :class="{ 'urgent': isExamUrgent(e.examTime) }">
          <div class="exam-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 20h9"></path>
              <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
            </svg>
          </div>
          <div class="exam-info">
            <div class="exam-name">{{ e.examName }}</div>
            <div v-if="e.courseName" class="exam-course">📚 {{ e.courseName }}</div>
            <div v-if="e.examTime" class="exam-time">⏰ {{ formatExamTime(e.examTime) }}</div>
            <div v-if="e.location" class="exam-location">📍 {{ e.location }}</div>
            <div v-if="e.duration" class="exam-duration">⏳ {{ e.duration }} 分钟</div>
          </div>
          <div class="exam-status">
            <div v-if="isExamPassed(e.examTime)" class="status-badge passed">已结束</div>
            <div v-else-if="isExamUrgent(e.examTime)" class="status-badge urgent">即将开始</div>
            <div v-else class="status-badge upcoming">未开始</div>
            <div v-if="e.examTime" class="exam-countdown">
              {{ getExamCountdown(e.examTime) }}
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="empty-state">
        <div class="empty-icon">📄</div>
        <h4>暂无考试</h4>
        <p>当前没有考试安排</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from "vue";
import request from "@/utils/request";

const courses = ref([]);
const exams = ref([]);
const loadingCourses = ref(false);
const loadingExams = ref(false);

// 计算属性：按考试时间排序
const sortedExams = computed(() => {
  return [...exams.value].sort((a, b) => {
    const timeA = a.examTime ? new Date(a.examTime).getTime() : 0;
    const timeB = b.examTime ? new Date(b.examTime).getTime() : 0;
    return timeA - timeB;
  });
});

// 方法
const loadCourses = async () => {
  loadingCourses.value = true;
  try {
    const res = await request.get("/api/student/course/page", { 
      params: { current: 1, size: 20 } 
    });
    courses.value = res.data?.records || [];
  } catch (error) {
    console.error("加载课程失败:", error);
  } finally {
    loadingCourses.value = false;
  }
};

const loadExams = async () => {
  loadingExams.value = true;
  try {
    const res = await request.get("/api/student/exam/page", { 
      params: { current: 1, size: 20 } 
    });
    exams.value = res.data?.records || [];
  } catch (error) {
    console.error("加载考试失败:", error);
  } finally {
    loadingExams.value = false;
  }
};

// 格式化考试时间
const formatExamTime = (time) => {
  if (!time) return "时间待定";
  const date = new Date(time);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).replace(/\//g, '-');
};

// 判断考试是否已结束
const isExamPassed = (examTime) => {
  if (!examTime) return false;
  const examDate = new Date(examTime);
  const now = new Date();
  return examDate < now;
};

// 判断考试是否紧急（3天内）
const isExamUrgent = (examTime) => {
  if (!examTime) return false;
  const examDate = new Date(examTime);
  const now = new Date();
  const diff = examDate.getTime() - now.getTime();
  const days = diff / (1000 * 60 * 60 * 24);
  return days <= 3 && days > 0;
};

// 获取考试倒计时
const getExamCountdown = (examTime) => {
  if (!examTime) return "";
  const examDate = new Date(examTime);
  const now = new Date();
  const diff = examDate.getTime() - now.getTime();
  
  if (diff <= 0) return "已结束";
  
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
  
  if (days > 0) return `${days}天后`;
  if (hours > 0) return `${hours}小时后`;
  if (minutes > 0) return `${minutes}分钟后`;
  return "即将开始";
};

// 根据分数获取样式类
const getScoreClass = (score) => {
  const numScore = parseFloat(score);
  if (isNaN(numScore)) return "";
  if (numScore >= 90) return "score-excellent";
  if (numScore >= 80) return "score-good";
  if (numScore >= 60) return "score-pass";
  return "score-fail";
};

onMounted(() => {
  loadCourses();
  loadExams();
});
</script>

<style scoped lang="less">
.schedule-page {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
  background: #f8f9ff;
  min-height: 100vh;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
    padding: 0 16px 24px;
  }
}

/* 通用卡片样式 */
.schedule-card {
  background: white;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 8px 32px rgba(106, 85, 255, 0.08);
  }
}

.course-card {
  .card-header {
    margin-bottom: 20px;
  }
}

.exam-card {
  .card-header {
    margin-bottom: 20px;
  }
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.header-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(106, 85, 255, 0.1), rgba(138, 122, 255, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #6a55ff;
  flex-shrink: 0;
}

.header-content {
  flex: 1;
  min-width: 0;
}

.header-content h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 4px;
  letter-spacing: -0.3px;
}

.header-content p {
  font-size: 14px;
  color: #666;
  margin: 0;
  line-height: 1.5;
  opacity: 0.9;
}

.refresh-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: white;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;
  
  &:hover {
    background: rgba(106, 85, 255, 0.08);
    color: #6a55ff;
  }
  
  svg {
    stroke: currentColor;
  }
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(106, 85, 255, 0.1);
  border-top-color: #6a55ff;
  border-radius: 50%;
  margin-bottom: 16px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-state p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

/* 课程列表 */
.course-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 500px;
  overflow-y: auto;
  padding-right: 8px;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: rgba(0, 0, 0, 0.05);
    border-radius: 3px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(106, 85, 255, 0.3);
    border-radius: 3px;
  }
}

.course-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  background: rgba(248, 247, 255, 0.5);
  border-radius: 16px;
  border: 1px solid rgba(106, 85, 255, 0.1);
  transition: all 0.3s ease;
  position: relative;
  
  &:hover {
    background: white;
    border-color: rgba(106, 85, 255, 0.2);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(106, 85, 255, 0.1);
  }
}

.course-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  
  svg {
    stroke: white;
  }
}

.course-info {
  flex: 1;
  min-width: 0;
}

.course-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
  line-height: 1.4;
}

.course-teacher,
.course-credit,
.course-time,
.course-location {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.course-score {
  position: absolute;
  top: 20px;
  right: 20px;
  font-size: 18px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 8px;
  
  &.score-excellent {
    background: rgba(78, 205, 196, 0.1);
    color: #4ecdc4;
    border: 1px solid rgba(78, 205, 196, 0.2);
  }
  
  &.score-good {
    background: rgba(106, 85, 255, 0.1);
    color: #6a55ff;
    border: 1px solid rgba(106, 85, 255, 0.2);
  }
  
  &.score-pass {
    background: rgba(255, 230, 109, 0.1);
    color: #d4a017;
    border: 1px solid rgba(255, 230, 109, 0.2);
  }
  
  &.score-fail {
    background: rgba(255, 107, 107, 0.1);
    color: #ff6b6b;
    border: 1px solid rgba(255, 107, 107, 0.2);
  }
}

/* 考试列表 */
.exam-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 500px;
  overflow-y: auto;
  padding-right: 8px;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: rgba(0, 0, 0, 0.05);
    border-radius: 3px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(106, 85, 255, 0.3);
    border-radius: 3px;
  }
}

.exam-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  background: rgba(248, 247, 255, 0.5);
  border-radius: 16px;
  border: 1px solid rgba(106, 85, 255, 0.1);
  transition: all 0.3s ease;
  position: relative;
  
  &:hover {
    background: white;
    border-color: rgba(106, 85, 255, 0.2);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(106, 85, 255, 0.1);
  }
  
  &.urgent {
    background: rgba(255, 230, 109, 0.1);
    border-color: rgba(255, 230, 109, 0.3);
    
    &:hover {
      background: rgba(255, 230, 109, 0.15);
    }
  }
}

.exam-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  
  svg {
    stroke: white;
  }
}

.exam-info {
  flex: 1;
  min-width: 0;
}

.exam-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
  line-height: 1.4;
}

.exam-course,
.exam-time,
.exam-location,
.exam-duration {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.exam-status {
  position: absolute;
  top: 20px;
  right: 20px;
  text-align: right;
}

.status-badge {
  font-size: 12px;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 8px;
  margin-bottom: 6px;
  
  &.passed {
    background: rgba(78, 205, 196, 0.1);
    color: #4ecdc4;
    border: 1px solid rgba(78, 205, 196, 0.2);
  }
  
  &.urgent {
    background: rgba(255, 107, 107, 0.1);
    color: #ff6b6b;
    border: 1px solid rgba(255, 107, 107, 0.2);
  }
  
  &.upcoming {
    background: rgba(106, 85, 255, 0.1);
    color: #6a55ff;
    border: 1px solid rgba(106, 85, 255, 0.2);
  }
}

.exam-countdown {
  font-size: 12px;
  color: #999;
  font-weight: 500;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: rgba(248, 247, 255, 0.5);
  border-radius: 12px;
  border: 2px dashed rgba(106, 85, 255, 0.2);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state h4 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.empty-state p {
  color: #666;
  margin: 0;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .schedule-card {
    padding: 20px;
    border-radius: 16px;
  }
  
  .header-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .header-content h3 {
    font-size: 18px;
  }
  
  .course-item,
  .exam-item {
    padding: 16px;
  }
  
  .course-icon,
  .exam-icon {
    width: 40px;
    height: 40px;
  }
  
  .course-score {
    position: static;
    align-self: center;
  }
  
  .exam-status {
    position: static;
    text-align: left;
    margin-top: 8px;
  }
}
</style>