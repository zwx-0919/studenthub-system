<template>
  <div class="chat-page">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <div class="header-left">
        <button class="back-btn" @click="goBack" title="返回">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12 19 5 12 12 5"></polyline>
          </svg>
        </button>
        <div class="user-info">
          <div v-if="receiver" class="user-avatar">
            {{ receiver.userName?.slice(0, 1) || "?" }}
          </div>
          <div class="user-details">
            <h3>{{ receiver?.userName || "未知用户" }}</h3>
            <div class="user-role">{{ roleText(receiver?.role) }}</div>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <button class="action-btn" title="刷新聊天记录" @click="load">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 4 23 10 17 10"></polyline>
            <polyline points="1 20 1 14 7 14"></polyline>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
          </svg>
        </button>
        <button class="action-btn danger" title="清空聊天记录" @click="clearChat">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            <line x1="10" y1="11" x2="10" y2="17"></line>
            <line x1="14" y1="11" x2="14" y2="17"></line>
          </svg>
        </button>
      </div>
    </div>

    <!-- 聊天记录区域 -->
    <div class="chat-container" ref="chatContainer">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载聊天记录...</p>
      </div>

      <div v-else-if="list.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <h3>开始对话</h3>
        <p>这是你与 {{ receiver?.userName || "对方" }} 的对话开始</p>
        <p class="empty-hint">发送消息开始交流</p>
      </div>

      <div v-else class="chat-history">
        <div v-for="(m, index) in list" :key="m.id" class="message-wrapper">
          <!-- 时间分割线 -->
          <div v-if="shouldShowDateDivider(list, index)" class="date-divider">
            <span>{{ formatDateDivider(m.sendTime) }}</span>
          </div>

          <!-- 对方消息 -->
          <div v-if="m.senderId !== store.user?.id" class="message-item other-message">
            <div class="message-left">
              <div class="message-avatar">
                {{ receiver?.userName?.slice(0, 1) || "?" }}
              </div>
              <div class="message-content">
                <div class="message-text">{{ m.content }}</div>
                <div class="message-time">{{ formatTime(m.sendTime) }}</div>
              </div>
            </div>
          </div>

          <!-- 我的消息 -->
          <div v-else class="message-item my-message">
            <div class="message-right">
              <div class="message-content">
                <div class="message-text">{{ m.content }}</div>
                <div class="message-time">
                  {{ formatTime(m.sendTime) }}
                  <svg v-if="m.isRead === 1" class="read-icon" viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 6L9 17l-5-5"></path>
                  </svg>
                  <svg v-else class="unread-icon" viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                  </svg>
                </div>
              </div>
              <div class="message-avatar">
                {{ store.user?.userName?.slice(0, 1) || "我" }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 发送消息区域 -->
    <div class="chat-input-area">
      <div class="input-wrapper">
        <textarea
          v-model="text"
          placeholder="输入消息..."
          @keydown.enter.exact.prevent="send"
          rows="1"
          ref="messageInput"
        />
        <div class="input-actions">
          <button 
            class="send-btn" 
            @click="send"
            :disabled="!text.trim()"
            :class="{ 'disabled': !text.trim() }"
          >
            <svg class="send-icon" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="22" y1="2" x2="11" y2="13"></line>
              <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, nextTick, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/store/user";
import request from "@/utils/request";
import { connectWs, closeWs } from "@/utils/ws";
import { ElMessage, ElMessageBox } from "element-plus";

const route = useRoute();
const router = useRouter();
const store = useUserStore();

const list = ref([]);
const text = ref("");
const loading = ref(false);
const receiver = ref(null);
const chatContainer = ref(null);
const messageInput = ref(null);
let timer = null;

// 格式化时间显示
const formatTime = (v) => {
  if (!v) return "";
  const date = new Date(v);
  const now = new Date();
  const diff = now - date;
  
  // 如果是今天，只显示时间
  if (diff < 24 * 60 * 60 * 1000 && date.getDate() === now.getDate()) {
    return date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  }
  
  // 如果是昨天
  if (diff < 2 * 24 * 60 * 60 * 1000 && date.getDate() === now.getDate() - 1) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
  }
  
  // 一周内
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    return `${days[date.getDay()]} ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
  }
  
  // 更早的时间
  return String(v).replace("T", " ").slice(0, 16);
};

// 日期分割线
const formatDateDivider = (v) => {
  if (!v) return "";
  const date = new Date(v);
  const now = new Date();
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  
  if (date.toDateString() === now.toDateString()) {
    return "今天";
  } else if (date.toDateString() === yesterday.toDateString()) {
    return "昨天";
  } else {
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }
};

// 判断是否显示日期分割线
const shouldShowDateDivider = (messages, index) => {
  if (index === 0) return true;
  const currentDate = new Date(messages[index].sendTime).toDateString();
  const prevDate = new Date(messages[index - 1].sendTime).toDateString();
  return currentDate !== prevDate;
};

// 获取角色文本
const roleText = (role) => {
  if (role === 1) return "学生";
  if (role === 2) return "辅导员";
  if (role === 3) return "管理员";
  return "用户";
};

// 加载聊天记录
const load = async () => {
  loading.value = true;
  try {
    const res = await request.get(`/api/private-chat/conversation/${route.params.id}`);
    // 将消息按时间升序排列（最早的在前）
    const messages = res.data || [];
    list.value = messages.sort((a, b) => {
      return new Date(a.sendTime) - new Date(b.sendTime);
    });
    
    // 滚动到底部
    nextTick(() => {
      scrollToBottom();
    });
    
    // 加载接收者信息
    loadReceiverInfo();
  } catch (error) {
    console.error("加载聊天记录失败:", error);
  } finally {
    loading.value = false;
  }
};

// 加载接收者信息
const loadReceiverInfo = async () => {
  try {
    const res = await request.get(`/user/simple/${route.params.id}`);
    receiver.value = res.data;
  } catch (error) {
    console.error("加载接收者信息失败:", error);
  }
};

// 发送消息
const send = async () => {
  if (!text.value.trim()) return;
  
  const message = text.value.trim();
  text.value = "";
  
  try {
    await request.post("/api/private-chat/send", {
      receiverId: Number(route.params.id),
      content: message
    });
    
    // 重新加载消息
    await load();
    
    // 自动滚动到底部
    scrollToBottom();
    
    // 聚焦输入框
    messageInput.value?.focus();
  } catch (error) {
    console.error("发送消息失败:", error);
    ElMessage.error("发送失败，请重试");
  }
};

// 清空聊天记录
const clearChat = async () => {
  try {
    await ElMessageBox.confirm(
      "确定要清空与这个用户的所有聊天记录吗？此操作不可恢复。",
      "确认清空",
      {
        confirmButtonText: "确定清空",
        cancelButtonText: "取消",
        type: "warning"
      }
    );
    
    // 这里调用清空聊天记录的接口
    // await request.delete(`/api/private-chat/clear/${route.params.id}`);
    
    list.value = [];
    ElMessage.success("已清空聊天记录");
  } catch (error) {
    // 用户取消
  }
};

// 滚动到底部
const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  }
};

// 返回
const goBack = () => {
  router.back();
};

onMounted(() => {
  load();
  messageInput.value?.focus();
  
  // 设置定时刷新
  timer = setInterval(load, 5000);
  
  // WebSocket 连接
  connectWs((msg) => {
    if (msg.type === 0) return;
    if (Number(msg.type) === 2 || String(msg.text).includes("私信")) {
      load();
    }
  });
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
  closeWs();
});

// 监听消息列表变化，自动滚动
watch(list, () => {
  nextTick(() => {
    scrollToBottom();
  });
}, { deep: true });
</script>

<style scoped lang="less">
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f8f9ff;
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
}

/* 聊天头部 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
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

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 18px;
  box-shadow: 0 4px 12px rgba(106, 85, 255, 0.3);
}

.user-details h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 4px;
}

.user-role {
  font-size: 12px;
  color: #666;
  padding: 2px 8px;
  border-radius: 8px;
  background: rgba(106, 85, 255, 0.1);
  color: #6a55ff;
  font-weight: 500;
  display: inline-block;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
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
  
  &.danger:hover {
    background: rgba(255, 107, 107, 0.08);
    color: #ff6b6b;
  }
  
  svg {
    stroke: currentColor;
  }
}

/* 聊天容器 */
.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: #f8f9ff;
  position: relative;
  
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

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #666;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.empty-state p {
  margin: 0 0 4px;
  line-height: 1.6;
}

.empty-hint {
  font-size: 13px;
  color: #999;
}

/* 聊天历史 */
.chat-history {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.date-divider {
  text-align: center;
  margin: 20px 0;
  position: relative;
  
  span {
    display: inline-block;
    padding: 6px 16px;
    background: rgba(0, 0, 0, 0.05);
    border-radius: 20px;
    font-size: 12px;
    color: #999;
    font-weight: 500;
  }
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    right: 0;
    top: 50%;
    height: 1px;
    background: rgba(0, 0, 0, 0.1);
    z-index: -1;
  }
}

.message-item {
  margin-bottom: 8px;
  display: flex;
  
  &.other-message {
    justify-content: flex-start;
  }
  
  &.my-message {
    justify-content: flex-end;
  }
}

.message-left {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 70%;
}

.message-right {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 70%;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
  margin-top: 4px;
}

.my-message .message-avatar {
  background: linear-gradient(135deg, #4ecdc4, #44a08d);
}

.message-content {
  max-width: calc(100% - 44px);
}

.message-text {
  background: white;
  border-radius: 18px;
  padding: 12px 16px;
  font-size: 15px;
  line-height: 1.5;
  color: #1a1a2e;
  word-break: break-word;
  border: 1px solid rgba(0, 0, 0, 0.05);
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  
  .message-left & {
    border-top-left-radius: 4px;
  }
  
  .message-right & {
    border-top-right-radius: 4px;
    background: linear-gradient(135deg, #6a55ff, #8a7aff);
    color: white;
    border: none;
  }
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
  padding: 0 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  
  .message-right & {
    justify-content: flex-end;
  }
  
  .read-icon {
    stroke: #4ecdc4;
  }
  
  .unread-icon {
    stroke: #999;
  }
}

/* 输入区域 */
.chat-input-area {
  padding: 20px 24px;
  background: white;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  position: relative;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: rgba(248, 247, 255, 0.5);
  border-radius: 20px;
  padding: 8px 8px 8px 20px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  
  &:focus-within {
    border-color: #6a55ff;
    box-shadow: 0 0 0 3px rgba(106, 85, 255, 0.1);
    background: white;
  }
}

textarea {
  flex: 1;
  min-height: 24px;
  max-height: 120px;
  border: none;
  padding: 8px 0;
  font-size: 15px;
  font-family: inherit;
  resize: none;
  background: transparent;
  line-height: 1.5;
  
  &:focus {
    outline: none;
  }
  
  &::placeholder {
    color: #999;
  }
}

.input-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.send-btn {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(106, 85, 255, 0.3);
  
  &:hover:not(.disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(106, 85, 255, 0.4);
  }
  
  &:active:not(.disabled) {
    transform: translateY(0);
  }
  
  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
  
  .send-icon {
    stroke: white;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-page {
    border-radius: 0;
    height: 100vh;
  }
  
  .chat-header {
    padding: 12px 16px;
  }
  
  .chat-container {
    padding: 16px;
  }
  
  .message-left,
  .message-right {
    max-width: 85%;
  }
  
  .chat-input-area {
    padding: 16px;
  }
  
  .user-avatar {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }
  
  .user-details h3 {
    font-size: 16px;
  }
}
</style>