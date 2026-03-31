<template>
  <!-- 模板部分保持不变 -->
  <div class="student-shell">
    <header class="top">
      <div class="page-container bar">
        <div class="left">
          <GlobalBackButton />
          <div class="logo">高校学生智能服务系统</div>
        </div>
        <div class="right">
          <nav>
            <router-link to="/student/home">首页</router-link>
            <router-link to="/student/forum">论坛</router-link>
            <router-link to="/student/check">打卡</router-link>
            <router-link to="/student/leave">请假</router-link>
            <router-link to="/student/course-exam">课程考试</router-link>
          </nav>
          <router-link class="msg-btn" to="/student/message" title="消息中心">
            <svg class="icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
            </svg>
            <span class="dot" v-if="store.unreadCount > 0"></span>
          </router-link>
          <div class="user-box" @mouseenter="showMenu = true" @mouseleave="showMenu = false">
            <div class="avatar">{{ avatarText }}</div>
            <span class="name">{{ store.user?.userName || "用户" }}</span>
            <Transition name="fade">
              <div class="menu" v-if="showMenu">
                <button @click="goAI">
                  <svg class="menu-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 2a2 2 0 1 0 0 4 2 2 0 0 0 0-4zM5 20v-2a7 7 0 0 1 14 0v2"></path>
                    <path d="M5 20h14"></path>
                  </svg>
                  AI助手
                </button>
                <button @click="goProfile">
                  <svg class="menu-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                  个人信息
                </button>
                <div class="menu-divider"></div>
                <button class="logout-btn" @click="logout">
                  <svg class="menu-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                    <polyline points="16 17 21 12 16 7"></polyline>
                    <line x1="21" y1="12" x2="9" y2="12"></line>
                  </svg>
                  退出登录
                </button>
              </div>
            </Transition>
          </div>
        </div>
      </div>
    </header>
    <main class="page-container body">
      <router-view />
    </main>

    <button class="ai-float" @click="goAI" title="AI助手">
      <svg class="ai-icon" viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M20.5 6.5L19.5 5.5L18.5 6.5L17.5 5.5L16.5 6.5L15.5 5.5"></path>
        <path d="M4 20v-1a4 4 0 0 1 4-4h8a4 4 0 0 1 4 4v1"></path>
        <circle cx="12" cy="8" r="3"></circle>
      </svg>
    </button>

    <MessagePopup
      :visible="popupVisible"
      :text="popupText"
      :unread="popupUnread"
      @detail="viewDetail"
      @close="closePopup"
    />
  </div>
</template>

<script setup>
// 脚本部分保持不变
import { computed, onMounted, onUnmounted, ref } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store/user";
import { closeWs, connectWs } from "@/utils/ws";
import MessagePopup from "@/components/MessagePopup.vue";
import GlobalBackButton from "@/components/GlobalBackButton.vue";

const store = useUserStore();
const router = useRouter();
const showMenu = ref(false);
const popupVisible = ref(false);
const popupUnread = ref(false);
const popupText = ref("");
const popupMsg = ref(null);

const avatarText = computed(() => {
  const name = store.user?.userName || "学";
  return name.slice(0, 1).toUpperCase();
});

const pushPopup = (msg) => {
  popupMsg.value = msg || null;
  popupText.value = String(msg?.text || "");
  popupUnread.value = true;
  popupVisible.value = true;
};

const closePopup = () => {
  popupVisible.value = false;
};

const viewDetail = () => {
  popupUnread.value = false;
  popupVisible.value = false;
  const msg = popupMsg.value;
  // type: 1系统 2私信 3互动
  if (Number(msg?.type) === 2 && msg?.bizId) {
    router.push(`/student/chat/${msg.bizId}`);
    return;
  }
  if (Number(msg?.type) === 3 && msg?.bizId) {
    router.push(`/student/post/${msg.bizId}`);
    return;
  }
  router.push("/student/message");
};

const logout = () => {
  closeWs();
  store.logout();
  router.replace("/login");
};

const goAI = () => {
  router.push("/student/ai");
};

const goProfile = () => {
  router.push("/student/profile");
};

onMounted(() => {
  connectWs((msg) => {
    if (msg.type === 0) return;
    pushPopup(msg);
    store.loadUnreadCount();
  });
});

onUnmounted(() => {
  closeWs();
});
</script>

<style scoped lang="less">
.student-shell {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9ff 0%, #f5f7ff 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
}

.top {
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid rgba(124, 92, 255, 0.08);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.02), 0 2px 8px rgba(124, 92, 255, 0.03);
}

.bar {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
}

.left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo {
  color: #6a55ff;
  font-weight: 700;
  font-size: 20px;
  letter-spacing: -0.3px;
  background: linear-gradient(135deg, #6a55ff 0%, #8a7aff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  padding-left: 4px;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 20px;
    background: linear-gradient(135deg, #6a55ff, #8a7aff);
    border-radius: 2px;
  }
}

.right {
  display: flex;
  align-items: center;
  gap: 20px;
}

nav {
  display: flex;
  gap: 4px;
  background: rgba(248, 247, 255, 0.8);
  border-radius: 12px;
  padding: 4px;
  border: 1px solid rgba(124, 92, 255, 0.06);
}

nav a {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  text-decoration: none;
  position: relative;
  
  &:hover {
    color: #6a55ff;
    background: rgba(255, 255, 255, 0.9);
    box-shadow: 0 2px 8px rgba(106, 85, 255, 0.1);
  }
  
  &.router-link-active {
    color: #6a55ff;
    font-weight: 600;
    background: white;
    box-shadow: 0 2px 8px rgba(106, 85, 255, 0.12);
    
    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 16px;
      right: 16px;
      height: 2px;
      background: linear-gradient(90deg, #6a55ff, #8a7aff);
      border-radius: 1px;
    }
  }
}

.msg-btn {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(248, 247, 255, 0.8);
  border: 1px solid rgba(124, 92, 255, 0.08);
  transition: all 0.2s ease;
  color: #666;
  
  &:hover {
    background: white;
    border-color: rgba(124, 92, 255, 0.2);
    color: #6a55ff;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(106, 85, 255, 0.1);
  }
  
  .icon {
    transition: transform 0.2s ease;
  }
  
  &:hover .icon {
    transform: scale(1.1);
  }
}

.dot {
  position: absolute;
  right: 8px;
  top: 8px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  box-shadow: 0 0 0 2px white;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 77, 79, 0.4);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(255, 77, 79, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(255, 77, 79, 0);
  }
}

.user-box {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px 6px 6px;
  border-radius: 16px;
  background: rgba(248, 247, 255, 0.8);
  border: 1px solid rgba(124, 92, 255, 0.08);
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 120px;
  
  &:hover {
    background: white;
    border-color: rgba(124, 92, 255, 0.2);
    box-shadow: 0 4px 12px rgba(106, 85, 255, 0.08);
  }
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  box-shadow: 0 2px 8px rgba(106, 85, 255, 0.3);
  flex-shrink: 0;
}

.name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  min-width: 0;
}

.menu {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 12px;
  width: 180px;
  padding: 8px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1), 0 2px 8px rgba(106, 85, 255, 0.05);
  z-index: 1001;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -5px;
    right: 20px;
    width: 10px;
    height: 10px;
    background: white;
    transform: rotate(45deg);
    border-top: 1px solid rgba(0, 0, 0, 0.06);
    border-left: 1px solid rgba(0, 0, 0, 0.06);
  }
}

.menu button {
  width: 100%;
  border: 0;
  border-radius: 8px;
  padding: 10px 12px;
  margin-bottom: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 400;
  color: #333;
  background: transparent;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.2s ease;
  text-align: left;
  
  &:hover {
    background: rgba(248, 247, 255, 0.8);
    color: #6a55ff;
    transform: translateX(2px);
    
    .menu-icon {
      stroke: #6a55ff;
    }
  }
  
  &:last-child {
    margin-bottom: 0;
  }
}

.menu-icon {
  stroke-width: 2;
  transition: stroke 0.2s ease;
  flex-shrink: 0;
}

.menu-divider {
  height: 1px;
  background: rgba(0, 0, 0, 0.06);
  margin: 8px 0;
}

.logout-btn {
  color: #ff4d4f;
  
  &:hover {
    background: rgba(255, 77, 79, 0.08) !important;
    color: #ff4d4f !important;
    
    .menu-icon {
      stroke: #ff4d4f;
    }
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
  transform-origin: top right;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(-8px);
}

.body {
  padding: 32px 0 40px;
  max-width: 1280px;
  margin: 0 auto;
  min-height: calc(100vh - 64px);
}

.ai-float {
  position: fixed;
  right: 32px;
  bottom: 32px;
  width: 60px;
  height: 60px;
  border-radius: 18px;
  border: none;
  cursor: pointer;
  box-shadow: 0 8px 32px rgba(106, 85, 255, 0.25), 0 2px 8px rgba(106, 85, 255, 0.15);
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  
  &:hover {
    transform: translateY(-4px) scale(1.05);
    box-shadow: 0 12px 40px rgba(106, 85, 255, 0.35), 0 4px 16px rgba(106, 85, 255, 0.2);
  }
  
  &:active {
    transform: translateY(-2px) scale(0.98);
  }
  
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: inherit;
    background: linear-gradient(135deg, transparent, rgba(255, 255, 255, 0.1));
  }
}

.ai-icon {
  stroke-width: 1.5;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

/* 响应式调整 */
@media (max-width: 768px) {
  .bar {
    padding: 0 16px;
  }
  
  nav {
    display: none;
  }
  
  .logo {
    font-size: 18px;
  }
  
  .ai-float {
    right: 20px;
    bottom: 20px;
    width: 56px;
    height: 56px;
  }
}
</style>