<template>
  <div class="message-center">
    <!-- 头部区域 -->
    <div class="message-header">
      <div class="header-content">
        <h1>📨 消息中心</h1>
        <p>查看和管理您的系统通知、私信和互动消息</p>
      </div>
      <div class="header-actions">
        <button class="header-btn" @click="load" title="刷新消息">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 4 23 10 17 10"></polyline>
            <polyline points="1 20 1 14 7 14"></polyline>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
          </svg>
          刷新
        </button>
        <div class="unread-badge" v-if="store.unreadCount > 0">
          <span>{{ store.unreadCount }} 条未读</span>
        </div>
      </div>
    </div>

    <!-- 发送消息区域 -->
    <div class="send-section">
      <div class="section-header">
        <h3>📤 发送私信</h3>
        <div class="message-tips">
          <svg class="tip-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="16" x2="12" y2="12"></line>
            <line x1="12" y1="8" x2="12.01" y2="8"></line>
          </svg>
          <span>仅支持发送私信给系统内注册用户</span>
        </div>
      </div>
      
      <div class="send-form">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">🔍 搜索接收人</label>
            <div class="search-wrapper">
              <input 
                v-model="searchName" 
                placeholder="输入姓名搜索接收人..." 
                class="search-input"
                @input="searchUsers"
              />
              <div class="search-results" v-if="users.length > 0 && searchName">
                <div 
                  v-for="u in users" 
                  :key="u.id"
                  class="user-item"
                  @click="selectUser(u)"
                >
                  <div class="user-avatar">{{ u.userName?.slice(0, 1) }}</div>
                  <div class="user-info">
                    <div class="user-name">{{ u.userName }}</div>
                    <div class="user-id">ID: {{ u.id }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="form-group">
            <label class="form-label">👤 选择接收人</label>
            <div class="select-wrapper">
              <select v-model.number="sendForm.receiverId" class="form-select">
                <option :value="null">请选择接收人</option>
                <option v-for="u in filteredUsers" :key="u.id" :value="u.id">{{ u.userName }}</option>
              </select>
              <div class="selected-user" v-if="selectedUser">
                <div class="selected-avatar">{{ selectedUser.userName?.slice(0, 1) }}</div>
                <div class="selected-name">{{ selectedUser.userName }}</div>
                <button class="clear-btn" @click="clearSelected" title="清除选择">×</button>
              </div>
            </div>
          </div>
        </div>
        
        <div class="form-group">
          <label class="form-label">💬 消息内容</label>
          <textarea 
            v-model="sendForm.content" 
            placeholder="请输入私信内容..." 
            class="message-textarea"
            rows="4"
          />
          <div class="textarea-footer">
            <div class="char-count">{{ sendForm.content.length }}/500</div>
            <button 
              class="send-btn" 
              @click="send"
              :disabled="!canSend"
              :class="{ 'disabled': !canSend }"
            >
              <svg v-if="!sending" class="send-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="22" y1="2" x2="11" y2="13"></line>
                <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
              </svg>
              <div v-else class="spinner"></div>
              {{ sending ? "发送中..." : "发送私信" }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="messages-section">
      <div class="section-header">
        <h3>📋 消息列表 ({{ list.length }})</h3>
        <div class="filter-tabs">
          <button 
            v-for="tab in tabs" 
            :key="tab.value"
            class="filter-tab"
            :class="{ active: activeTab === tab.value }"
            @click="activeTab = tab.value"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>
      
      <div v-if="filteredMessages.length > 0" class="messages-list">
        <div v-for="m in filteredMessages" :key="m.id" class="message-item" :class="{ unread: m.isRead === 0 }">
          <div class="message-icon" :class="getMessageClass(m.messageType)">
            {{ getMessageIcon(m.messageType) }}
          </div>
          <div class="message-content">
            <div class="message-header">
              <div class="message-sender">
                <span class="sender-name">{{ m.senderName || "系统" }}</span>
                <span class="message-type">{{ typeText(m.messageType) }}</span>
                <span class="message-time">{{ formatTime(m.createTime) }}</span>
              </div>
              <div class="message-actions">
                <button 
                  v-if="m.isRead === 0"
                  class="read-btn"
                  @click="markRead(m.id)"
                  title="标记为已读"
                >
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 6L9 17l-5-5"></path>
                  </svg>
                  标记已读
                </button>
                <button 
                  class="reply-btn"
                  v-if="m.messageType === 2"
                  @click="replyTo(m)"
                  title="回复"
                >
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 17l-5-5 5-5"></path>
                    <path d="M20 18v-2a4 4 0 00-4-4H4"></path>
                  </svg>
                  回复
                </button>
              </div>
            </div>
            <div class="message-text">{{ m.content }}</div>
            <div class="message-footer">
              <div class="message-id">ID: {{ m.id }}</div>
              <div class="message-status" :class="{ read: m.isRead === 1 }">
                {{ m.isRead === 1 ? '已读' : '未读' }}
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="empty-state">
        <div class="empty-icon">📭</div>
        <h3>暂无消息</h3>
        <p>当前没有{{ activeTab === 0 ? '' : tabs.find(t => t.value === activeTab)?.label.toLowerCase() }}消息</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, computed, watch } from "vue";
import request from "@/utils/request";
import { useUserStore } from "@/store/user";
import { connectWs } from "@/utils/ws";
import { ElMessage } from "element-plus";

const store = useUserStore();
const list = ref([]);
const users = ref([]);
const searchName = ref("");
const sending = ref(false);
const activeTab = ref(0); // 0: 全部, 1: 系统, 2: 私信, 3: 互动
const sendForm = reactive({ receiverId: null, content: "", messageType: 2, bizId: null });

const tabs = [
  { label: "全部消息", value: 0 },
  { label: "系统通知", value: 1 },
  { label: "私信", value: 2 },
  { label: "互动提醒", value: 3 }
];

// 计算属性
const canSend = computed(() => {
  return sendForm.receiverId && sendForm.content.trim().length > 0 && sendForm.content.length <= 500;
});

const filteredUsers = computed(() => {
  if (!searchName.value) return [];
  return users.value;
});

const selectedUser = computed(() => {
  if (!sendForm.receiverId) return null;
  return users.value.find(u => u.id === sendForm.receiverId) || null;
});

const filteredMessages = computed(() => {
  if (activeTab.value === 0) return list.value;
  return list.value.filter(m => m.messageType === activeTab.value);
});

// 方法
const load = async () => {
  try {
    const res = await request.get("/api/message/page", { params: { current: 1, size: 50 } });
    list.value = res.data?.records || [];
    store.loadUnreadCount();
  } catch (error) {
    ElMessage.error("加载消息失败");
  }
};

const send = async () => {
  if (!canSend.value || sending.value) return;
  
  sending.value = true;
  try {
    await request.post("/api/message/send", sendForm);
    ElMessage.success("私信发送成功");
    sendForm.content = "";
    sendForm.receiverId = null;
    searchName.value = "";
    load();
  } catch (error) {
    ElMessage.error(error?.message || "发送失败");
  } finally {
    sending.value = false;
  }
};

const markRead = async (id) => {
  try {
    await request.patch(`/api/message/read/${id}`);
    await load();
    ElMessage.success("标记为已读");
  } catch (error) {
    ElMessage.error("操作失败");
  }
};

const typeText = (t) => {
  const types = { 1: "系统通知", 2: "私信", 3: "互动提醒" };
  return types[t] || "未知";
};

const getMessageIcon = (t) => {
  const icons = { 1: "🔔", 2: "💬", 3: "💡" };
  return icons[t] || "📧";
};

const getMessageClass = (t) => {
  const classes = { 1: "system", 2: "private", 3: "interaction" };
  return classes[t] || "";
};

const formatTime = (time) => {
  if (!time) return "刚刚";
  const date = new Date(time);
  const now = new Date();
  const diff = now - date;
  
  if (diff < 60000) return "刚刚";
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;
  
  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const searchUsers = async () => {
  if (!searchName.value.trim()) {
    users.value = [];
    return;
  }
  try {
    const res = await request.get("/user/search", { params: { keyword: searchName.value } });
    users.value = res.data || [];
  } catch (error) {
    users.value = [];
  }
};

const selectUser = (user) => {
  sendForm.receiverId = user.id;
  searchName.value = "";
};

const clearSelected = () => {
  sendForm.receiverId = null;
};

const replyTo = (message) => {
  if (message.senderId) {
    sendForm.receiverId = message.senderId;
    const user = users.value.find(u => u.id === message.senderId);
    if (user) {
      searchName.value = user.userName;
    }
  }
  document.querySelector('.message-textarea')?.focus();
};

onMounted(() => {
  load();
  connectWs(() => load());
});

// 监听内容长度
watch(() => sendForm.content, (content) => {
  if (content.length > 500) {
    sendForm.content = content.substring(0, 500);
  }
});
</script>

<style scoped lang="less">
.message-center {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px 40px;
  background: #f8f9ff;
  min-height: 100vh;
}

/* 头部区域 */
.message-header {
  background: white;
  border-radius: 24px;
  padding: 32px 40px;
  margin: 24px 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #6a55ff, #8a7aff);
  }
}

.header-content h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px;
  letter-spacing: -0.3px;
}

.header-content p {
  font-size: 15px;
  color: #666;
  margin: 0;
  line-height: 1.6;
  opacity: 0.9;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 12px;
  border: 1px solid rgba(106, 85, 255, 0.2);
  background: rgba(106, 85, 255, 0.08);
  color: #6a55ff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    background: rgba(106, 85, 255, 0.15);
    transform: translateY(-1px);
  }
  
  svg {
    stroke: currentColor;
  }
}

.unread-badge {
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}

/* 发送消息区域 */
.send-section {
  background: white;
  border-radius: 24px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}

.section-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #999;
  
  .tip-icon {
    stroke: #999;
  }
}

/* 表单样式 */
.send-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.form-group {
  position: relative;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.search-wrapper {
  position: relative;
}

.search-input {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 0 16px;
  font-size: 15px;
  transition: all 0.3s ease;
  background: rgba(248, 247, 255, 0.5);
  
  &:focus {
    outline: none;
    border-color: #6a55ff;
    box-shadow: 0 0 0 3px rgba(106, 85, 255, 0.1);
    background: white;
  }
  
  &::placeholder {
    color: #999;
  }
}

.search-results {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  z-index: 10;
  max-height: 200px;
  overflow-y: auto;
  display: none;
  
  .search-input:focus + & {
    display: block;
  }
  
  &:hover {
    display: block;
  }
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  
  &:hover {
    background: rgba(106, 85, 255, 0.08);
  }
  
  &:last-child {
    border-bottom: none;
  }
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 15px;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
  margin-bottom: 2px;
}

.user-id {
  font-size: 12px;
  color: #999;
}

.select-wrapper {
  position: relative;
}

.form-select {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 0 16px;
  font-size: 15px;
  background: rgba(248, 247, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%23666' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 16px center;
  background-size: 16px;
  padding-right: 40px;
  
  &:focus {
    outline: none;
    border-color: #6a55ff;
    box-shadow: 0 0 0 3px rgba(106, 85, 255, 0.1);
    background-color: white;
  }
}

.selected-user {
  display: none;
  position: absolute;
  top: 1px;
  left: 1px;
  right: 1px;
  bottom: 1px;
  background: white;
  border-radius: 11px;
  padding: 0 16px;
  align-items: center;
  gap: 12px;
  
  .form-select:focus + & {
    display: flex;
  }
}

.selected-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #4ecdc4, #44a08d);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 15px;
  flex-shrink: 0;
}

.selected-name {
  flex: 1;
  font-size: 15px;
  color: #1a1a2e;
  font-weight: 500;
}

.clear-btn {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.1);
  color: #666;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  
  &:hover {
    background: rgba(255, 107, 107, 0.2);
    color: #ff6b6b;
  }
}

.message-textarea {
  width: 100%;
  min-height: 120px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 16px;
  font-size: 15px;
  font-family: inherit;
  resize: vertical;
  transition: all 0.3s ease;
  background: rgba(248, 247, 255, 0.5);
  line-height: 1.6;
  
  &:focus {
    outline: none;
    border-color: #6a55ff;
    box-shadow: 0 0 0 3px rgba(106, 85, 255, 0.1);
    background: white;
  }
  
  &::placeholder {
    color: #999;
  }
}

.textarea-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  flex-wrap: wrap;
  gap: 12px;
}

.char-count {
  font-size: 13px;
  color: #999;
  
  &:global(.warning) {
    color: #ff6b6b;
  }
}

.send-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 28px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 120px;
  
  &:hover:not(.disabled) {
    transform: translateY(-1px);
    box-shadow: 0 8px 20px rgba(106, 85, 255, 0.3);
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

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 消息列表区域 */
.messages-section {
  background: white;
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
}

.filter-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-tab {
  padding: 8px 16px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: white;
  color: #666;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    background: rgba(106, 85, 255, 0.08);
    color: #6a55ff;
  }
  
  &.active {
    background: rgba(106, 85, 255, 0.15);
    color: #6a55ff;
    border-color: rgba(106, 85, 255, 0.3);
  }
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
}

.message-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  background: rgba(248, 247, 255, 0.5);
  transition: all 0.3s ease;
  position: relative;
  
  &:hover {
    background: white;
    border-color: rgba(106, 85, 255, 0.1);
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(106, 85, 255, 0.1);
  }
  
  &.unread {
    background: rgba(106, 85, 255, 0.08);
    border-color: rgba(106, 85, 255, 0.2);
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: #6a55ff;
      border-radius: 4px 0 0 4px;
    }
  }
}

.message-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  
  &.system {
    background: linear-gradient(135deg, #ffe66d, #ffb142);
  }
  
  &.private {
    background: linear-gradient(135deg, #6a55ff, #8a7aff);
  }
  
  &.interaction {
    background: linear-gradient(135deg, #4ecdc4, #44a08d);
  }
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 12px;
}

.message-sender {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.sender-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
}

.message-type {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 8px;
  background: rgba(106, 85, 255, 0.1);
  color: #6a55ff;
  font-weight: 500;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-actions {
  display: flex;
  gap: 8px;
}

.read-btn,
.reply-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: white;
  color: #666;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    transform: translateY(-1px);
  }
  
  svg {
    stroke: currentColor;
  }
}

.read-btn {
  color: #6a55ff;
  
  &:hover {
    background: rgba(106, 85, 255, 0.08);
  }
}

.reply-btn {
  color: #4ecdc4;
  
  &:hover {
    background: rgba(78, 205, 196, 0.08);
  }
}

.message-text {
  font-size: 15px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 12px;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  font-size: 12px;
  color: #999;
}

.message-status {
  &.read {
    color: #4ecdc4;
  }
  
  &:not(.read) {
    color: #ff6b6b;
    font-weight: 500;
  }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: rgba(248, 247, 255, 0.5);
  border-radius: 20px;
  border: 2px dashed rgba(106, 85, 255, 0.2);
  margin-top: 20px;
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
  color: #666;
  margin: 0;
  line-height: 1.6;
  max-width: 400px;
  margin: 0 auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-center {
    padding: 0 16px 24px;
  }
  
  .message-header {
    padding: 24px;
    border-radius: 20px;
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
    margin: 16px 0;
  }
  
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .send-section,
  .messages-section {
    padding: 24px;
    border-radius: 20px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .filter-tabs {
    width: 100%;
    overflow-x: auto;
    padding-bottom: 8px;
    
    &::-webkit-scrollbar {
      height: 4px;
    }
    
    &::-webkit-scrollbar-track {
      background: rgba(0, 0, 0, 0.05);
    }
    
    &::-webkit-scrollbar-thumb {
      background: rgba(106, 85, 255, 0.3);
      border-radius: 2px;
    }
  }
  
  .message-item {
    padding: 16px;
  }
  
  .message-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .message-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>