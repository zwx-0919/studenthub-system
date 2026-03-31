<template>
  <div class="student-home">
    <!-- 轮播公告区域 -->
    <section class="carousel-section">
      <div class="section-header">
        <h2 class="section-title">📢 校园公告</h2>
        <router-link to="/student/notice" class="view-all">查看全部</router-link>
      </div>
      <div class="carousel-container">
        <div
          class="slide clickable"
          :style="{ backgroundImage: `url(${currentNotice.imageUrl})` }"
          @click="goNoticeDetail"
        >
          <div class="gradient-overlay"></div>
          <div class="slide-content">
            <div class="notice-badge">最新公告</div>
            <h3 class="notice-title">{{ currentNotice.title }}</h3>
            <p class="notice-desc">{{ currentNotice.content }}</p>
          </div>
        </div>
        <div class="carousel-controls">
          <button class="control-btn prev" @click="prevSlide" title="上一个">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="15 18 9 12 15 6"></polyline>
            </svg>
          </button>
          <div class="dots">
            <span
              v-for="(_, i) in notices"
              :key="i"
              :class="{ active: i === activeIndex }"
              @click="activeIndex = i"
            />
          </div>
          <button class="control-btn next" @click="nextSlide" title="下一个">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="9 18 15 12 9 6"></polyline>
            </svg>
          </button>
        </div>
      </div>
    </section>

    <!-- 快捷功能入口 -->
    <section class="features-section">
      <h2 class="section-title">🚀 快捷功能</h2>
      <div class="features-grid">
        <router-link class="feature-card" to="/student/check">
          <div class="feature-icon">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
              <path d="M9 12l2 2 4-4"></path>
            </svg>
          </div>
          <div class="feature-content">
            <h3>学业打卡</h3>
            <p>记录学习进度，养成好习惯</p>
          </div>
          <div class="feature-arrow">→</div>
        </router-link>

        <router-link class="feature-card" to="/student/leave">
          <div class="feature-icon">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
              <path d="M8 14h.01"></path>
              <path d="M12 14h.01"></path>
              <path d="M16 14h.01"></path>
              <path d="M8 18h.01"></path>
              <path d="M12 18h.01"></path>
              <path d="M16 18h.01"></path>
            </svg>
          </div>
          <div class="feature-content">
            <h3>请假申请</h3>
            <p>在线申请，快速审批</p>
          </div>
          <div class="feature-arrow">→</div>
        </router-link>

        <router-link class="feature-card" to="/student/forum">
          <div class="feature-icon">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v10z"></path>
              <line x1="9" y1="10" x2="15" y2="10"></line>
              <line x1="12" y1="7" x2="12" y2="13"></line>
            </svg>
          </div>
          <div class="feature-content">
            <h3>校园论坛</h3>
            <p>交流学习，分享生活</p>
          </div>
          <div class="feature-arrow">→</div>
        </router-link>

        <router-link class="feature-card" to="/student/course-exam">
          <div class="feature-icon">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"></path>
              <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"></path>
            </svg>
          </div>
          <div class="feature-content">
            <h3>课程考试</h3>
            <p>课程安排，考试提醒</p>
          </div>
          <div class="feature-arrow">→</div>
        </router-link>
      </div>
    </section>

    <!-- 温馨提示区域 -->
    <section class="reminders-section">
      <div class="reminder-card">
        <div class="reminder-header">
          <div class="reminder-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10"></circle>
              <polyline points="12 6 12 12 16 14"></polyline>
            </svg>
          </div>
          <h3>📅 即将开始的课程</h3>
        </div>
        <div class="reminder-content">
          <p v-if="nearestCourse" class="reminder-text">{{ nearestCourse }}</p>
          <p v-else class="reminder-placeholder">暂无24小时内课程安排</p>
          <div class="reminder-footer">
            <span class="reminder-hint">提前5分钟进入教室</span>
          </div>
        </div>
      </div>

      <div class="reminder-card">
        <div class="reminder-header">
          <div class="reminder-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"></path>
            </svg>
          </div>
          <h3>📝 考试倒计时</h3>
        </div>
        <div class="reminder-content">
          <p v-if="nearestExam" class="reminder-text">{{ nearestExam }}</p>
          <p v-else class="reminder-placeholder">暂无一周内考试安排</p>
          <div class="reminder-footer">
            <span class="reminder-hint">建议提前复习备考</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import request from "@/utils/request";
import { useUserStore } from "@/store/user";

const store = useUserStore();
const router = useRouter();
const activeIndex = ref(0);
const defaultBannerUrl =
  "https://java-web-zwx.oss-cn-beijing.aliyuncs.com/20250905214631_%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_2025-08-26_122856_580.jpg";
const notices = ref([
  {
    title: "欢迎使用高校学生智能系统",
    content: "系统已全面升级，为您提供更优质的服务体验",
    imageUrl: defaultBannerUrl
  }
]);
const reminderData = ref({ courses: [], exams: [] });
let timer = null;

const currentNotice = computed(
  () =>
    notices.value[activeIndex.value] || {
      title: "欢迎使用高校学生智能系统",
      content: "系统已全面升级，为您提供更优质的服务体验",
      imageUrl: defaultBannerUrl
    }
);
const nearestCourse = computed(() => reminderData.value.courses?.[0] || "");
const nearestExam = computed(() => reminderData.value.exams?.[0] || "");

const prevSlide = () => {
  activeIndex.value = activeIndex.value > 0 ? activeIndex.value - 1 : notices.value.length - 1;
};

const nextSlide = () => {
  activeIndex.value = (activeIndex.value + 1) % notices.value.length;
};

watch(
  () => notices.value.length,
  (len) => {
    if (len <= 1) return;
    if (timer) clearInterval(timer);
    timer = setInterval(() => {
      nextSlide();
    }, 5000);
  },
  { immediate: true }
);

onMounted(async () => {
  try {
    const [remindRes, noticeRes] = await Promise.all([
      request.get("/api/student/remind/upcoming"),
      request.get("/notice/notice", { params: { current: 1, size: 5, status: 1 } })
    ]);
    reminderData.value = {
      courses: remindRes.data.courses || [],
      exams: remindRes.data.exams || []
    };
    const records = noticeRes.data?.records || [];
    if (records.length) {
      notices.value = records.map((n) => ({
        id: n.id,
        title: n.title || "校园公告",
        content: n.content || "暂无内容",
        imageUrl: n.coverUrl || n.imageUrl || defaultBannerUrl
      }));
    }
  } catch (_) {
    // ignore
  }
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});

const goNoticeDetail = () => {
  if (!currentNotice.value?.id) return;
  router.push(`/student/notice/${currentNotice.value.id}`);
};
</script>

<style scoped lang="less">
.student-home {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(135deg, #f8f9ff 0%, #f5f7ff 100%);
  min-height: calc(100vh - 64px);
}

/* ===================== 区块标题样式 ===================== */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.view-all {
  color: #6a55ff;
  font-size: 14px;
  text-decoration: none;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.2s ease;
  
  &:hover {
    background: rgba(106, 85, 255, 0.08);
    transform: translateX(2px);
  }
}

/* ===================== 轮播公告区域 ===================== */
.carousel-section {
  margin-bottom: 32px;
}

.carousel-container {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(106, 85, 255, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    box-shadow: 0 12px 40px rgba(106, 85, 255, 0.12);
    transform: translateY(-2px);
  }
}

.slide {
  position: relative;
  height: 300px;
  padding: 40px 48px;
  background-size: cover;
  background-position: center;
  background-color: #1a1a2e;
  cursor: pointer;
  display: flex;
  align-items: center;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(26, 26, 46, 0.8) 0%, rgba(106, 85, 255, 0.6) 100%);
  }
}

.gradient-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to right, rgba(0, 0, 0, 0.7) 0%, transparent 50%);
  z-index: 1;
}

.slide-content {
  position: relative;
  z-index: 2;
  max-width: 60%;
  color: white;
}

.notice-badge {
  display: inline-block;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(106, 85, 255, 0.3);
}

.notice-title {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.3;
  margin: 0 0 16px;
  letter-spacing: -0.3px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.notice-desc {
  font-size: 16px;
  line-height: 1.6;
  opacity: 0.9;
  margin: 0 0 24px;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
  -webkit-line-clamp: 3;  // 修改这里的数字
}

.notice-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  opacity: 0.8;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.carousel-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.4), transparent);
  z-index: 3;
}

.control-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  
  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: scale(1.1);
  }
  
  &.prev {
    margin-right: auto;
  }
  
  &.next {
    margin-left: auto;
  }
}

.dots {
  display: flex;
  gap: 8px;
  margin: 0 20px;
}

.dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    background: rgba(255, 255, 255, 0.5);
    transform: scale(1.2);
  }
  
  &.active {
    width: 20px;
    background: white;
    border-radius: 10px;
  }
}

/* ===================== 功能入口区域 ===================== */
.features-section {
  margin-bottom: 32px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 12px;
}

.feature-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  text-decoration: none;
  color: inherit;
  border: 1px solid rgba(106, 85, 255, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(106, 85, 255, 0.3), transparent);
  }
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(106, 85, 255, 0.12);
    border-color: rgba(106, 85, 255, 0.2);
    
    .feature-icon {
      transform: scale(1.1) rotate(5deg);
    }
    
    .feature-arrow {
      transform: translateX(4px);
      color: #6a55ff;
    }
  }
}

.feature-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(106, 85, 255, 0.1), rgba(138, 122, 255, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6a55ff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
}

.feature-content {
  flex: 1;
  min-width: 0;
}

.feature-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px;
  letter-spacing: -0.3px;
}

.feature-content p {
  font-size: 13px;
  color: #666;
  margin: 0;
  line-height: 1.5;
  opacity: 0.8;
}

.feature-arrow {
  font-size: 20px;
  color: #999;
  transition: all 0.2s ease;
  opacity: 0.6;
}

/* ===================== 温馨提示区域 ===================== */
.reminders-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.reminder-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(106, 85, 255, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(106, 85, 255, 0.08);
  }
}

.reminder-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.reminder-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(106, 85, 255, 0.1), rgba(138, 122, 255, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6a55ff;
}

.reminder-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
  letter-spacing: -0.3px;
}

.reminder-content {
  min-height: 100px;
}

.reminder-text {
  font-size: 16px;
  line-height: 1.6;
  color: #333;
  margin: 0 0 16px;
  font-weight: 500;
}

.reminder-placeholder {
  font-size: 14px;
  color: #999;
  font-style: italic;
  margin: 0 0 16px;
  opacity: 0.7;
}

.reminder-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.reminder-hint {
  font-size: 12px;
  color: #6a55ff;
  padding: 4px 12px;
  background: rgba(106, 85, 255, 0.08);
  border-radius: 12px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .student-home {
    padding: 16px;
  }
  
  .slide {
    height: 240px;
    padding: 24px;
  }
  
  .slide-content {
    max-width: 100%;
  }
  
  .notice-title {
    font-size: 22px;
  }
  
  .notice-desc {
    font-size: 14px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .reminders-section {
    grid-template-columns: 1fr;
  }
  
  .carousel-controls {
    padding: 16px;
  }
}
</style>