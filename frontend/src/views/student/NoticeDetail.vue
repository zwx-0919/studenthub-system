<template>
  <div class="notice-detail" v-if="notice">
    <!-- 公告头部区域 -->
    <div class="notice-header" :class="{ 'no-hero-img': !headerCoverSrc }">
      <div class="header-background" v-if="headerCoverSrc">
        <img 
          class="cover" 
          :src="headerCoverSrc" 
          :alt="notice.title"
        />
        <div class="cover-overlay"></div>
      </div>
      
      <div class="header-content">
        <h1 class="notice-title">{{ notice.title }}</h1>
        
        <div class="author-info">
          <div class="author-avatar">
            {{ (notice.authorName || "系").slice(0, 1) }}
          </div>
          <div class="author-details">
            <div class="author-name">{{ notice.authorName || "系统管理员" }}</div>
            <div class="author-role">管理员</div>
          </div>
        </div>
        
        <div class="meta-info">
          <div class="meta-item">
            <svg class="meta-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <polyline points="12 6 12 12 16 14"></polyline>
            </svg>
            <span class="meta-text">{{ formatTime(notice.publishTime) }}</span>
          </div>
          
          <div class="meta-item">
            <svg class="meta-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
              <circle cx="12" cy="12" r="3"></circle>
            </svg>
            <span class="meta-text">{{ notice.viewCount || 0 }} 次浏览</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 公告内容区域 -->
    <div class="notice-content">
      <div class="content-wrapper">
        <div class="content-header">
          <h3 class="content-title">📄 公告详情</h3>
        </div>
        
        <div class="content-text">{{ notice.content }}</div>

        <div v-if="singleNoticeImage" class="content-images">
          <el-image
            class="content-img"
            :src="singleNoticeImage"
            fit="contain"
            preview-teleported
            :preview-src-list="[singleNoticeImage]"
          />
        </div>
        
        <div class="content-footer">
          <div class="important-tip">
            <svg class="tip-icon" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            <span>请仔细阅读以上内容，如有疑问请及时联系管理员</span>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 加载状态 -->
  <div class="loading-state" v-else>
    <div class="loading-content">
      <div class="loading-spinner"></div>
      <p>正在加载公告详情...</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import request from "@/utils/request";

const route = useRoute();
const notice = ref(null);

const noticeImageList = computed(() => {
  const u = notice.value?.imageUrl;
  if (!u) return [];
  return String(u).split(",").map((x) => x.trim()).filter(Boolean);
});

/** 学生端：仅 1 张图展示；≥2 张不展示任何图片 */
const headerCoverSrc = computed(() => {
  const list = noticeImageList.value;
  if (list.length === 1) return list[0];
  return null;
});

const singleNoticeImage = computed(() => {
  const list = noticeImageList.value;
  if (list.length === 1) return list[0];
  return null;
});

// 格式化时间
const formatTime = (v) => {
  if (!v) return "刚刚";
  const date = new Date(v);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).replace(/\//g, '-');
};

onMounted(async () => {
  try {
    const res = await request.get(`/notice/detail/${route.params.id}`);
    notice.value = res.data;
  } catch (error) {
    console.error("加载公告详情失败:", error);
  }
});
</script>

<style scoped lang="less">
.notice-header.no-hero-img {
  padding-top: 32px;
  background: linear-gradient(135deg, #6a55ff 0%, #8a7aff 100%);
}

.notice-detail {
  max-width: 1000px;
  margin: 0 auto;
  background: #f8f9ff;
  min-height: 100vh;
  border-radius: 0;
  padding: 0;
  overflow: hidden;
}

/* 头部区域 */
.notice-header {
  position: relative;
  margin-bottom: 24px;
  border-radius: 0 0 24px 24px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.header-background {
  position: relative;
  height: 300px;
  overflow: hidden;
}

.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
  
  .header-background:hover & {
    transform: scale(1.05);
  }
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.2), rgba(0, 0, 0, 0.6));
}

.header-content {
  position: relative;
  padding: 40px 40px 32px;
  background: white;
  border-radius: 24px 24px 0 0;
  margin-top: -24px;
  z-index: 1;
}

.notice-title {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.4;
  color: #1a1a2e;
  margin: 0 0 24px;
  letter-spacing: -0.3px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #4ecdc4, #44a08d);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.author-details {
  flex: 1;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.author-role {
  font-size: 12px;
  color: #666;
  background: rgba(106, 85, 255, 0.08);
  padding: 3px 10px;
  border-radius: 10px;
  display: inline-block;
}

.meta-info {
  display: flex;
  gap: 24px;
  align-items: center;
  flex-wrap: wrap;
  padding-top: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(248, 247, 255, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(106, 85, 255, 0.1);
  transition: all 0.3s ease;
  
  &:hover {
    background: white;
    border-color: rgba(106, 85, 255, 0.2);
    transform: translateY(-1px);
  }
}

.meta-icon {
  color: #6a55ff;
  flex-shrink: 0;
}

.meta-text {
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

/* 内容区域 */
.notice-content {
  background: white;
  border-radius: 24px;
  margin: 0 24px 24px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
}

.content-wrapper {
  padding: 40px;
}

.content-header {
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.content-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-text {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
  word-break: break-word;
  margin-bottom: 40px;
  padding: 0 4px;
}

.content-footer {
  padding-top: 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
}

.important-tip {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: rgba(255, 107, 107, 0.08);
  border-radius: 12px;
  border: 1px solid rgba(255, 107, 107, 0.2);
  color: #ff6b6b;
  font-size: 14px;
  font-weight: 500;
}

.tip-icon {
  stroke: #ff6b6b;
  flex-shrink: 0;
}

/* 加载状态 */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  background: #f8f9ff;
}

.loading-content {
  text-align: center;
  padding: 40px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid rgba(106, 85, 255, 0.1);
  border-top-color: #6a55ff;
  border-radius: 50%;
  margin: 0 auto 20px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-content p {
  color: #666;
  font-size: 15px;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .notice-header {
    border-radius: 0 0 20px 20px;
  }
  
  .header-background {
    height: 200px;
  }
  
  .header-content {
    padding: 24px 20px;
    margin-top: -16px;
    border-radius: 20px 20px 0 0;
  }
  
  .notice-title {
    font-size: 24px;
  }
  
  .author-info {
    gap: 12px;
  }
  
  .author-avatar {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }
  
  .author-name {
    font-size: 15px;
  }
  
  .author-role {
    font-size: 11px;
  }
  
  .meta-info {
    gap: 12px;
  }
  
  .meta-item {
    padding: 6px 12px;
  }
  
  .meta-text {
    font-size: 13px;
  }
  
  .notice-content {
    margin: 0 16px 16px;
    border-radius: 20px;
  }
  
  .content-wrapper {
    padding: 24px;
  }
  
  .content-text {
    font-size: 15px;
  }
  
  .important-tip {
    padding: 12px 16px;
    font-size: 13px;
  }
}
</style>