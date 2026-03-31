<template>
  <div class="user-profile-page">
    <div v-if="user" class="profile-container">
      <!-- 头部区域 -->
      <div class="profile-header">
        <div class="header-background">
          <div class="bg-overlay"></div>
        </div>
        
        <div class="header-content">
          <div class="user-avatar-section">
            <div class="avatar-container">
              <div v-if="user.avatar" class="avatar-img-wrapper">
                <img :src="user.avatar" class="avatar-img" :alt="user.userName" />
              </div>
              <div v-else class="avatar-circle">
                {{ (user.userName || "用").slice(0, 1) }}
              </div>
              <div class="online-status" :class="{ 'online': isOnline }"></div>
            </div>
            
            <div class="user-title">
              <h1>{{ user.userName }}</h1>
              <div class="user-badges">
                <div class="badge" :class="getRoleClass(user.role)">
                  {{ roleText(user.role) }}
                </div>
                <div v-if="user.college" class="badge college">
                  🎓 {{ user.college }}
                </div>
                <div v-if="user.major" class="badge major">
                  📚 {{ user.major }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 个人信息区域 -->
      <div class="profile-content">
        <div class="content-card">
          <div class="card-header">
            <h3>📄 个人信息</h3>
            <button class="edit-btn" title="编辑资料" v-if="user.id === currentUserId">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
              </svg>
            </button>
          </div>
          
          <div class="info-grid">
            <div class="info-item">
              <div class="info-label">
                <svg class="info-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
                用户名
              </div>
              <div class="info-value">{{ user.userName }}</div>
            </div>
            
            <div class="info-item">
              <div class="info-label">
                <svg class="info-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                  <line x1="16" y1="2" x2="16" y2="6"></line>
                  <line x1="8" y1="2" x2="8" y2="6"></line>
                  <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                注册时间
              </div>
              <div class="info-value">{{ formatTime(user.createTime) || '未知' }}</div>
            </div>
            
            <div v-if="user.phone" class="info-item">
              <div class="info-label">
                <svg class="info-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
                </svg>
                手机号
              </div>
              <div class="info-value">{{ user.phone }}</div>
            </div>
            
            <div v-if="user.email" class="info-item">
              <div class="info-label">
                <svg class="info-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                  <polyline points="22,6 12,13 2,6"></polyline>
                </svg>
                邮箱
              </div>
              <div class="info-value">{{ user.email }}</div>
            </div>
          </div>
          
          <div v-if="user.signature" class="user-signature">
            <div class="signature-label">个性签名</div>
            <div class="signature-text">"{{ user.signature }}"</div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <button class="chat-btn" @click="goChat">
            <svg class="chat-icon" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v10z"></path>
            </svg>
            发送私信
          </button>
        </div>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-else class="loading-state">
      <div class="loading-content">
        <div class="loading-spinner"></div>
        <p>加载用户信息...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import request from "@/utils/request";
import { useUserStore } from "@/store/user";

const route = useRoute();
const router = useRouter();
const store = useUserStore();
const user = ref(null);
const isOnline = ref(false);

// 计算当前用户ID
const currentUserId = computed(() => store.user?.id);

// 格式化时间
const formatTime = (time) => {
  if (!time) return "";
  const date = new Date(time);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-');
};

// 获取角色样式类
const getRoleClass = (role) => {
  const classes = { 1: "student", 2: "teacher", 3: "admin" };
  return classes[role] || "student";
};

const roleText = (role) => {
  if (role === 1) return "学生";
  if (role === 2) return "辅导员";
  if (role === 3) return "管理员";
  return "用户";
};

const goChat = () => {
  router.push(`/student/chat/${route.params.id}`);
};

onMounted(async () => {
  try {
    const res = await request.get(`/user/simple/${route.params.id}`);
    user.value = res.data;
  } catch (error) {
    console.error("加载用户信息失败:", error);
  }
});
</script>

<style scoped lang="less">
.user-profile-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px 40px;
  background: #f8f9ff;
  min-height: 100vh;
}

/* 头部区域 */
.profile-header {
  position: relative;
  background: white;
  border-radius: 24px;
  margin: 24px 0;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
}

.header-background {
  height: 120px;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  position: relative;
}

.bg-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.1), rgba(0, 0, 0, 0.3));
}

.header-content {
  position: relative;
  padding: 0 40px 40px;
  margin-top: -40px;
}

.user-avatar-section {
  display: flex;
  align-items: flex-end;
  gap: 24px;
  margin-bottom: 24px;
}

.avatar-container {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 24px;
  border: 4px solid white;
  background: white;
  box-shadow: 0 8px 32px rgba(106, 85, 255, 0.2);
  z-index: 1;
}

.avatar-img-wrapper {
  width: 100%;
  height: 100%;
  border-radius: 20px;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(106, 85, 255, 0.1), rgba(138, 122, 255, 0.1));
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-circle {
  width: 100%;
  height: 100%;
  border-radius: 20px;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: 700;
}

.online-status {
  position: absolute;
  bottom: 8px;
  right: 8px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #ccc;
  border: 3px solid white;
  transition: all 0.3s ease;
  
  &.online {
    background: #4ecdc4;
    box-shadow: 0 0 0 3px rgba(78, 205, 196, 0.2);
  }
}

.user-title {
  flex: 1;
  margin-bottom: 20px;
}

.user-title h1 {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 12px;
  letter-spacing: -0.3px;
}

.user-badges {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.badge {
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  
  &.student {
    background: rgba(106, 85, 255, 0.1);
    color: #6a55ff;
    border: 1px solid rgba(106, 85, 255, 0.2);
  }
  
  &.teacher {
    background: rgba(255, 230, 109, 0.1);
    color: #d4a017;
    border: 1px solid rgba(255, 230, 109, 0.2);
  }
  
  &.admin {
    background: rgba(255, 107, 107, 0.1);
    color: #ff6b6b;
    border: 1px solid rgba(255, 107, 107, 0.2);
  }
  
  &.college {
    background: rgba(78, 205, 196, 0.1);
    color: #4ecdc4;
    border: 1px solid rgba(78, 205, 196, 0.2);
  }
  
  &.major {
    background: rgba(138, 122, 255, 0.1);
    color: #8a7aff;
    border: 1px solid rgba(138, 122, 255, 0.2);
  }
}

/* 内容区域 */
.profile-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.content-card {
  background: white;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.card-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.edit-btn {
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
  
  &:hover {
    background: rgba(106, 85, 255, 0.08);
    color: #6a55ff;
  }
  
  svg {
    stroke: currentColor;
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  background: rgba(248, 247, 255, 0.5);
  border-radius: 12px;
  border: 1px solid rgba(106, 85, 255, 0.1);
  transition: all 0.3s ease;
  
  &:hover {
    background: white;
    border-color: rgba(106, 85, 255, 0.2);
    transform: translateY(-2px);
  }
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
  font-weight: 500;
  
  .info-icon {
    stroke: #666;
  }
}

.info-value {
  font-size: 16px;
  font-weight: 500;
  color: #1a1a2e;
  word-break: break-word;
}

.user-signature {
  padding: 20px;
  background: rgba(248, 247, 255, 0.8);
  border-radius: 12px;
  border: 1px dashed rgba(106, 85, 255, 0.2);
}

.signature-label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.signature-text {
  font-size: 15px;
  line-height: 1.6;
  color: #333;
  font-style: italic;
  font-weight: 500;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
}

.chat-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 24px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  box-shadow: 0 4px 12px rgba(106, 85, 255, 0.3);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(106, 85, 255, 0.4);
  }
  
  .chat-icon {
    stroke: white;
  }
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
  .user-profile-page {
    padding: 0 16px 24px;
  }
  
  .profile-header {
    border-radius: 20px;
    margin: 16px 0;
  }
  
  .header-content {
    padding: 0 20px 20px;
  }
  
  .user-avatar-section {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .avatar-container {
    width: 100px;
    height: 100px;
  }
  
  .avatar-circle {
    font-size: 40px;
  }
  
  .user-title h1 {
    font-size: 24px;
  }
  
  .content-card {
    padding: 20px;
    border-radius: 16px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>