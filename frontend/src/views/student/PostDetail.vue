<template>
  <div class="post-detail">
    <!-- 帖子详情卡片 -->
    <div class="post-card" v-if="post">
      <!-- 头部信息 -->
      <div class="post-header">
        <div class="user-info">
          <div 
            class="avatar clickable" 
            :style="{ background: getGradientColor(post.userId) }"
            @click="goUserHome(post.userId)"
          >
            {{ (post.userName || "匿").slice(0, 1).toUpperCase() }}
          </div>
          <div class="user-meta">
            <div class="user-name clickable" @click="goUserHome(post.userId)">
              {{ post.userName || "匿名用户" }}
              <span class="user-badge" v-if="post.userId === 1">👑 管理员</span>
            </div>
            <div class="post-meta">
              <span class="time">{{ formatTime(post.createTime) }}</span>
              <span class="divider">·</span>
              <span class="views">👁️ {{ post.viewCount || 0 }} 浏览</span>
            </div>
          </div>
        </div>
        <div class="post-actions">
          <button class="action-btn" title="收藏">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
            </svg>
          </button>
          <button class="action-btn" title="分享">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="18" cy="5" r="3"></circle>
              <circle cx="6" cy="12" r="3"></circle>
              <circle cx="18" cy="19" r="3"></circle>
              <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line>
              <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line>
            </svg>
          </button>
        </div>
      </div>

      <!-- 帖子内容 -->
      <div class="post-content">
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="content-text">{{ post.content }}</div>
        
        <!-- 图片展示 -->
        <div class="image-gallery" v-if="postImageList.length">
          <div 
            v-for="(img, idx) in postImageList" 
            :key="img + idx"
            class="image-item"
            :class="{ 'single': postImageList.length === 1, 'double': postImageList.length === 2, 'multiple': postImageList.length >= 3 }"
            @click="openPreview(idx)"
          >
            <el-image
              class="post-image"
              :src="img"
              :preview-src-list="postImageList"
              :initial-index="idx"
              fit="cover"
              preview-teleported
              lazy
            />
            <div class="image-overlay">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="white" stroke-width="2">
                <path d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"></path>
                <rect x="2" y="4" width="20" height="16" rx="2"></rect>
                <path d="M2 12h3"></path>
                <path d="M19 12h3"></path>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- 帖子统计 -->
      <div class="post-stats">
        <div class="stat-item">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
          </svg>
          <span>{{ post.likeCount || 0 }}</span>
        </div>
        <div class="stat-item">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v10z"></path>
          </svg>
          <span>{{ post.commentCount || 0 }}</span>
        </div>
      </div>
    </div>

    <!-- 评论区域 -->
    <div class="comments-section">
      <div class="section-header">
        <h3 class="section-title">💬 评论 ({{ comments.length }})</h3>
        <div class="comment-sort">
          <select v-model="sortBy" class="sort-select">
            <option value="time">最新</option>
            <option value="hot">热门</option>
          </select>
        </div>
      </div>

      <!-- 评论列表 -->
      <div class="comments-list">
        <div class="comment-item" v-for="c in comments" :key="c.id">
          <div class="comment-avatar clickable" @click="goUserHome(c.userId)" :style="{ background: getGradientColor(c.userId) }">
            {{ (userNameMap[c.userId] || "匿").slice(0, 1).toUpperCase() }}
          </div>
          <div class="comment-content">
            <div class="comment-header">
              <div class="comment-user">
                <span class="comment-name clickable" @click="goUserHome(c.userId)">
                  {{ userNameMap[c.userId] || "匿名用户" }}
                  <span class="user-badge" v-if="c.userId === 1">👑</span>
                </span>
                <span class="comment-time">{{ formatTime(c.createTime) }}</span>
              </div>
              <div class="comment-actions">
                <button class="comment-action-btn" title="回复">回复</button>
                <button class="comment-action-btn" title="点赞">👍 {{ c.likeCount || 0 }}</button>
              </div>
            </div>
            <div class="comment-text">{{ c.content }}</div>
          </div>
        </div>
        
        <div class="empty-state" v-if="!comments.length">
          <div class="empty-icon">💬</div>
          <p class="empty-text">暂无评论，快来发表第一条评论吧</p>
        </div>
      </div>
    </div>

    <!-- 固定评论输入框 -->
    <div class="comment-input-fixed">
      <div class="input-container">
        <input 
          v-model.trim="commentText" 
          placeholder="写下你的评论..." 
          class="comment-input"
          @keyup.enter="submitComment"
        />
        <button 
          class="submit-btn" 
          :class="{ 'disabled': !commentText.trim() }" 
          @click="submitComment"
          :disabled="!commentText.trim()"
        >
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="22" y1="2" x2="11" y2="13"></line>
            <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
          </svg>
          发送
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { useRouter } from "vue-router";
import request from "@/utils/request";
import { ElMessage } from "element-plus";

const route = useRoute();
const router = useRouter();
const post = ref(null);
const comments = ref([]);
const commentText = ref("");
const userNameMap = ref({});
const sortBy = ref("time");

// 生成用户头像渐变背景色
const getGradientColor = (userId) => {
  const colors = [
    "linear-gradient(135deg, #6a55ff, #8a7aff)",
    "linear-gradient(135deg, #ff6b6b, #ff8e8e)",
    "linear-gradient(135deg, #4ecdc4, #44a08d)",
    "linear-gradient(135deg, #ffe66d, #ffb142)",
    "linear-gradient(135deg, #a8e6cf, #7bc7a2)"
  ];
  const index = (userId || 0) % colors.length;
  return colors[index];
};

// 格式化时间
const formatTime = (raw) => {
  if (!raw) return "刚刚";
  const date = new Date(raw);
  const now = new Date();
  const diff = now - date;
  
  if (diff < 60000) return "刚刚";
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;
  
  return date.toLocaleDateString().replace(/\//g, "-");
};

// 获取图片列表
const postImageList = computed(() => {
  const imageUrl = post.value?.imageUrl || "";
  return String(imageUrl)
    .split(",")
    .map((x) => x.trim())
    .filter(Boolean);
});

// 打开图片预览
const openPreview = (index) => {
  // 已由el-image处理预览
};

// 加载帖子详情
const loadDetail = async () => {
  try {
    const res = await request.get(`/api/student/post/${route.params.id}`);
    post.value = res.data?.post || null;
    comments.value = res.data?.comments || [];

    // 收集所有用户ID
    const ids = Array.from(new Set((comments.value || []).map((c) => c.userId).filter(Boolean)));
    if (post.value?.userId) ids.push(post.value.userId);
    const uniqueIds = Array.from(new Set(ids));
    
    if (uniqueIds.length) {
      await loadUserNames(uniqueIds);
    }
  } catch (error) {
    ElMessage.error("加载帖子失败");
  }
};

// 加载用户名
const loadUserNames = async (ids) => {
  const tasks = ids.map(async (id) => {
    try {
      const res = await request.get(`/user/simple/${id}`);
      const name = res.data?.userName;
      if (name) userNameMap.value[id] = name;
    } catch (_) {
      // ignore
    }
  });
  await Promise.all(tasks);
};

// 提交评论
const submitComment = async () => {
  if (!commentText.value.trim()) {
    ElMessage.warning("请输入评论内容");
    return;
  }
  
  try {
    await request.post("/api/student/post/comment", {
      postId: Number(route.params.id),
      content: commentText.value.trim()
    });
    
    ElMessage.success("评论成功");
    commentText.value = "";
    await loadDetail();
  } catch (e) {
    const msg = e?.message || "评论失败";
    ElMessage.error(msg.includes("禁言") ? "您已被禁言，无法发布内容" : msg);
  }
};

// 跳转到用户主页
const goUserHome = (id) => {
  if (!id) return;
  router.push(`/student/user/${id}`);
};

onMounted(loadDetail);
</script>

<style scoped lang="less">
.post-detail {
  max-width: 760px;
  margin: 0 auto;
  padding: 24px 20px 100px;
  background: #f8f9ff;
  min-height: 100vh;
}

/* 帖子卡片 */
.post-card {
  background: white;
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 8px 32px rgba(106, 85, 255, 0.08);
  }
}

/* 帖子头部 */
.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(106, 85, 255, 0.2);
  
  &:hover {
    transform: scale(1.05);
  }
}

.clickable {
  cursor: pointer;
  
  &:hover {
    opacity: 0.8;
  }
}

.user-meta {
  flex: 1;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-badge {
  background: linear-gradient(135deg, #ffe66d, #ffb142);
  color: #333;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
}

.divider {
  opacity: 0.5;
}

.views {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: white;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  
  &:hover {
    background: rgba(106, 85, 255, 0.08);
    color: #6a55ff;
    transform: translateY(-1px);
  }
}

/* 帖子内容 */
.post-content {
  margin-bottom: 24px;
}

.post-title {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.4;
  color: #1a1a2e;
  margin: 0 0 20px;
  letter-spacing: -0.3px;
}

.content-text {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 24px;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 图片展示 */
.image-gallery {
  display: grid;
  gap: 12px;
  margin-top: 24px;
  
  &.single {
    grid-template-columns: 1fr;
  }
  
  &.double {
    grid-template-columns: repeat(2, 1fr);
  }
  
  &.multiple {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
}

.image-item {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    
    .image-overlay {
      opacity: 1;
    }
  }
}

.post-image {
  width: 100%;
  height: 300px;
  border-radius: 16px;
  object-fit: cover;
  display: block;
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

/* 帖子统计 */
.post-stats {
  display: flex;
  gap: 24px;
  padding-top: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  
  svg {
    stroke: #666;
  }
}

/* 评论区域 */
.comments-section {
  background: white;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
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

.comment-sort {
  position: relative;
}

.sort-select {
  padding: 8px 16px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: white;
  color: #333;
  font-size: 14px;
  cursor: pointer;
  outline: none;
  transition: all 0.2s ease;
  min-width: 100px;
  
  &:hover, &:focus {
    border-color: rgba(106, 85, 255, 0.5);
  }
}

/* 评论列表 */
.comments-list {
  margin-bottom: 24px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding: 20px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  
  &:last-child {
    border-bottom: none;
  }
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  flex-shrink: 0;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  &:hover {
    transform: scale(1.05);
  }
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.comment-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  
  &:hover {
    color: #6a55ff;
  }
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.comment-action-btn {
  padding: 4px 12px;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: white;
  color: #666;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    background: rgba(106, 85, 255, 0.08);
    color: #6a55ff;
  }
}

.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  color: #999;
  font-size: 15px;
  margin: 0;
}

/* 固定评论输入框 */
.comment-input-fixed {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  padding: 16px 20px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.08);
  z-index: 100;
}

.input-container {
  max-width: 760px;
  margin: 0 auto;
  display: flex;
  gap: 12px;
  align-items: center;
}

.comment-input {
  flex: 1;
  height: 48px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  padding: 0 20px;
  font-size: 15px;
  outline: none;
  transition: all 0.3s ease;
  background: rgba(248, 247, 255, 0.5);
  
  &:focus {
    border-color: #6a55ff;
    box-shadow: 0 0 0 3px rgba(106, 85, 255, 0.1);
  }
  
  &::placeholder {
    color: #999;
  }
}

.submit-btn {
  height: 48px;
  padding: 0 24px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  min-width: 100px;
  
  &:hover:not(.disabled) {
    transform: translateY(-1px);
    box-shadow: 0 4px 16px rgba(106, 85, 255, 0.3);
  }
  
  &:active:not(.disabled) {
    transform: translateY(0);
  }
  
  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .post-detail {
    padding: 16px 12px 100px;
  }
  
  .post-card,
  .comments-section {
    padding: 20px 16px;
    border-radius: 16px;
  }
  
  .post-title {
    font-size: 22px;
  }
  
  .content-text {
    font-size: 15px;
  }
  
  .comment-input-fixed {
    padding: 12px 16px;
  }
  
  .input-container {
    gap: 8px;
  }
  
  .comment-input {
    height: 44px;
    font-size: 14px;
  }
  
  .submit-btn {
    height: 44px;
    padding: 0 20px;
    min-width: 80px;
  }
  
  .post-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .post-actions {
    align-self: flex-end;
  }
}
</style>