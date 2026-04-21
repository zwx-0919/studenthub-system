<template>
  <div class="ai-page">
    <!-- 头部区域 -->
    <div class="ai-header">
      <div class="header-content">
        <div class="header-icon">🤖</div>
        <div class="header-text">
          <h1>AI 智能助手</h1>
          <p>为您提供课程、考试、时间、地点等安排查询服务</p>
        </div>
      </div>
      <div class="header-stats">
        <div class="stat-item">
          <div class="stat-number">{{ list.length }}</div>
          <div class="stat-label">对话记录</div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="ai-input-section">
      <div class="input-container">
        <div class="input-header">
          <div class="input-header-top">
            <h3 class="input-title">💬 请描述您的问题</h3>
            <button class="action-btn" @click="startNewSession" :disabled="thinking" title="开启新会话">
              新会话
            </button>
          </div>
          <div class="quick-questions">
            <span class="quick-label">快捷提问：</span>
            <div class="quick-buttons">
              <button 
                v-for="(q, idx) in quickQuestions" 
                :key="idx"
                class="quick-btn"
                @click="question = q"
                :disabled="thinking"
              >
                {{ q }}
              </button>
            </div>
          </div>
          <div class="session-tip">当前会话内支持多轮追问，例如“那周三呢”“这个考试地点在哪”。</div>
        </div>
        
        <div class="textarea-wrapper">
          <textarea 
            v-model="question" 
            placeholder="例如：我下周有哪些考试？明天的课程在哪上课？最近有什么重要通知？"
            :disabled="thinking"
            @keydown.enter.exact.prevent="ask"
            rows="4"
          />
          <div class="textarea-footer">
            <div class="hint-text">
              <svg class="hint-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
              </svg>
              <span>按 Enter 发送，Shift + Enter 换行</span>
            </div>
            <button 
              class="submit-btn" 
              :class="{ 'disabled': !question.trim() || thinking }" 
              @click="ask"
              :disabled="!question.trim() || thinking"
            >
              <svg v-if="!thinking" class="send-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="22" y1="2" x2="11" y2="13"></line>
                <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
              </svg>
              <div v-else class="spinner"></div>
              {{ thinking ? "思考中..." : "发送提问" }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 思考遮罩层 -->
    <div v-if="thinking" class="thinking-mask">
      <div class="thinking-content">
        <div class="thinking-animation">
          <div class="thinking-dot"></div>
          <div class="thinking-dot"></div>
          <div class="thinking-dot"></div>
        </div>
        <h3>AI 助手正在思考中</h3>
        <p>正在为您分析问题，请稍等片刻...</p>
      </div>
    </div>

    <!-- 历史记录 -->
    <div class="history-section">
      <div class="section-header">
        <div>
          <h2 class="section-title">📁 历史对话记录</h2>
          <p class="section-subtitle">按会话展示，支持连续追问</p>
        </div>
        <div class="section-actions">
          <button 
            class="action-btn" 
            @click="load"
            :disabled="thinking"
            title="刷新"
          >
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="23 4 23 10 17 10"></polyline>
              <polyline points="1 20 1 14 7 14"></polyline>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
            </svg>
            刷新
          </button>
          <button 
            v-if="list.length > 0"
            class="action-btn danger"
            @click="clearHistory"
            :disabled="thinking"
            title="清空记录"
          >
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              <line x1="10" y1="11" x2="10" y2="17"></line>
              <line x1="14" y1="11" x2="14" y2="17"></line>
            </svg>
            清空
          </button>
        </div>
      </div>
      
      <div v-if="chatSessions.length > 0" class="history-list chat-list">
        <div v-for="session in chatSessions" :key="session.sessionId" class="session-card">
          <div class="session-card-header">
            <div>
              <div class="session-title">会话 {{ session.sessionIndex }}</div>
              <div class="session-meta">{{ session.items.length }} 条消息 · {{ formatTime(session.items[0]?.createTime) }}</div>
            </div>
            <button class="small-btn" @click="useSession(session.sessionId)">继续此会话</button>
          </div>
          <div class="chat-thread">
            <template v-for="item in session.items" :key="item.id">
              <div class="chat-row user-row">
                <div class="chat-bubble user-bubble">
                  <div class="bubble-label">你</div>
                  <div class="bubble-text">{{ item.question }}</div>
                </div>
              </div>
              <div class="chat-row ai-row">
                <div class="chat-bubble ai-bubble">
                  <div class="bubble-label">AI</div>
                  <div class="bubble-text">{{ item.answer }}</div>
                  <div class="bubble-footer">
                    <span>{{ formatTime(item.createTime) }}</span>
                    <button class="small-btn ghost" title="复制回答" @click="copyToClipboard(item.answer)">复制</button>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
      
      <div v-else class="empty-state">
        <div class="empty-icon">💬</div>
        <h3>暂无对话记录</h3>
        <p>开始与 AI 助手对话，它会帮助您解答学习生活中的问题</p>
      </div>
    </div>
        </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import request from "@/utils/request";

const question = ref("");
const list = ref([]);
const thinking = ref(false);
const sessionId = ref(localStorage.getItem("ai-consult-session-id") || "");
const quickQuestions = ref([
  "我下周有哪些考试？",
  "明天的课程安排是什么？",
  "最近有什么重要通知？",
  "我这周的请假状态怎么样？"
]);

const currentSession = () => sessionId.value || "default";

const groupBySession = (items) => {
  const map = new Map();
  const order = [];
  items.forEach((item) => {
    const sid = item.sessionId || "default";
    if (!map.has(sid)) {
      map.set(sid, []);
      order.push(sid);
    }
    map.get(sid).push(item);
  });

  return order.map((sid, idx) => ({
    sessionId: sid,
    sessionIndex: idx + 1,
    items: map.get(sid).sort((a, b) => new Date(a.createTime) - new Date(b.createTime)),
  }));
};

const chatSessions = computed(() => groupBySession(list.value));

// 格式化时间
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

// 复制到剪贴板
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text);
    ElMessage.success("已复制到剪贴板");
  } catch (err) {
    // 降级方案
    const textArea = document.createElement("textarea");
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.select();
    document.execCommand("copy");
    document.body.removeChild(textArea);
    ElMessage.success("已复制到剪贴板");
  }
};

// 生成新会话
const startNewSession = () => {
  sessionId.value = window.crypto?.randomUUID ? window.crypto.randomUUID().replace(/-/g, "") : `${Date.now()}${Math.random().toString(16).slice(2)}`;
  localStorage.setItem("ai-consult-session-id", sessionId.value);
  ElMessage.success("已开启新会话");
};

const useSession = (sid) => {
  sessionId.value = sid || "";
  localStorage.setItem("ai-consult-session-id", sessionId.value);
  ElMessage.success("已切换到该会话");
};

// 加载历史记录
const load = async () => {
  try {
    const res = await request.get("/api/student/ai/page", { 
      params: { current: 1, size: 30 } 
    });
    list.value = res.data?.records || [];
    if (sessionId.value && !list.value.some((x) => x.sessionId === sessionId.value) && list.value.length > 0) {
      sessionId.value = list.value[0].sessionId || "";
      if (sessionId.value) localStorage.setItem("ai-consult-session-id", sessionId.value);
    }
  } catch (error) {
    console.error("加载历史记录失败:", error);
  }
};

// 提问
const ask = async () => {
  if (thinking.value) return;
  if (!question.value.trim()) {
    ElMessage.warning("请输入您的问题");
    return;
  }
  
  const q = question.value.trim();
  thinking.value = true;
  
  try {
    if (!sessionId.value) startNewSession();
    const res = await request.post("/api/student/ai/ask", 
      { question: q, sessionId: currentSession() }, 
      { timeout: 60000 }
    );
    
    const created = res.data;
    if (created) {
      // 添加到列表开头，并过滤掉可能重复的记录
      list.value = [created, ...list.value.filter((x) => x.id !== created.id)];
      if (created.sessionId) {
        sessionId.value = created.sessionId;
        localStorage.setItem("ai-consult-session-id", created.sessionId);
      }
    }
    
    question.value = "";
    ElMessage.success("AI 助手已回复");
  } catch (e) {
    const msg = e?.message || "提问失败，请稍后重试";
    if (msg.includes("timeout")) {
      ElMessage.warning("AI 响应时间较长，请稍后查看历史记录");
    } else {
      ElMessage.error(msg);
    }
  } finally {
    thinking.value = false;
  }
};

// 清空历史记录
const clearHistory = async () => {
  try {
    await ElMessageBox.confirm(
      "确定要清空所有对话记录吗？此操作不可撤销。",
      "确认清空",
      {
        confirmButtonText: "确定清空",
        cancelButtonText: "取消",
        type: "warning",
        confirmButtonClass: "danger"
      }
    );
    
    // 这里应该调用清空接口，如果没有就只清空本地
    // await request.delete("/api/student/ai/clear");
    list.value = [];
    sessionId.value = "";
    localStorage.removeItem("ai-consult-session-id");
    ElMessage.success("已清空对话记录");
  } catch {
    // 用户取消
  }
};

onMounted(load);
</script>

<style scoped lang="less">
.ai-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px 40px;
  background: #f8f9ff;
  min-height: 100vh;
}

/* 头部区域 */
.ai-header {
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

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
}

.header-icon {
  font-size: 40px;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  width: 72px;
  height: 72px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(106, 85, 255, 0.3);
}

.header-text h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px;
  letter-spacing: -0.3px;
}

.header-text p {
  font-size: 15px;
  color: #666;
  margin: 0;
  line-height: 1.6;
  opacity: 0.9;
}

.header-stats {
  display: flex;
  justify-content: flex-end;
  min-width: 120px;
}

.stat-item {
  text-align: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, rgba(106, 85, 255, 0.1), rgba(138, 122, 255, 0.1));
  border-radius: 16px;
  border: 1px solid rgba(106, 85, 255, 0.2);
  min-width: 100px;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(106, 85, 255, 0.15);
  }
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #6a55ff;
  margin-bottom: 4px;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* 输入区域 */
.ai-input-section {
  background: white;
  border-radius: 24px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
}

.input-container {
  position: relative;
}

.input-header {
  margin-bottom: 20px;
}

.input-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.quick-questions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.session-tip {
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(106, 85, 255, 0.06);
  color: #6a55ff;
  font-size: 13px;
  line-height: 1.5;
}

.quick-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  display: block;
  margin-bottom: 8px;
}

.quick-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-btn {
  padding: 8px 16px;
  border-radius: 12px;
  border: 1px solid rgba(106, 85, 255, 0.2);
  background: rgba(106, 85, 255, 0.08);
  color: #6a55ff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  
  &:hover:not(:disabled) {
    background: rgba(106, 85, 255, 0.15);
    transform: translateY(-1px);
  }
  
  &:active:not(:disabled) {
    transform: translateY(0);
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.textarea-wrapper {
  position: relative;
}

textarea {
  width: 100%;
  min-height: 140px;
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 20px;
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
  
  &:disabled {
    background: #f5f5f5;
    cursor: not-allowed;
  }
  
  &::placeholder {
    color: #999;
  }
}

.textarea-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.hint-text {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #999;
}

.hint-icon {
  stroke: #999;
  flex-shrink: 0;
}

.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 32px;
  border-radius: 16px;
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
}

.send-icon {
  stroke: white;
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

/* 思考遮罩层 */
.thinking-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.thinking-content {
  background: white;
  border-radius: 24px;
  padding: 40px;
  text-align: center;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.4s ease;
}

@keyframes slideUp {
  from { 
    opacity: 0;
    transform: translateY(20px);
  }
  to { 
    opacity: 1;
    transform: translateY(0);
  }
}

.thinking-animation {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;
}

.thinking-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  animation: bounce 1.4s infinite ease-in-out;
  
  &:nth-child(1) { animation-delay: -0.32s; }
  &:nth-child(2) { animation-delay: -0.16s; }
  &:nth-child(3) { animation-delay: 0s; }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.thinking-content h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.thinking-content p {
  color: #666;
  margin: 0;
  line-height: 1.6;
}

/* 历史记录区域 */
.history-section {
  background: white;
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.section-subtitle {
  margin: 6px 0 0;
  color: #888;
  font-size: 13px;
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

.section-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: white;
  color: #666;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover:not(:disabled) {
    background: rgba(106, 85, 255, 0.08);
    color: #6a55ff;
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
  
  &.danger {
    color: #ff6b6b;
    
    &:hover:not(:disabled) {
      background: rgba(255, 107, 107, 0.08);
      color: #ff6b6b;
    }
  }
  
  svg {
    stroke: currentColor;
  }
}

/* 历史列表 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chat-list {
  gap: 24px;
}

.session-card {
  border-radius: 20px;
  padding: 20px;
  border: 1px solid rgba(106, 85, 255, 0.08);
  background: rgba(248, 247, 255, 0.7);
}

.session-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.session-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a2e;
}

.session-meta {
  font-size: 12px;
  color: #888;
  margin-top: 4px;
}

.chat-thread {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.chat-row {
  display: flex;
}

.user-row {
  justify-content: flex-end;
}

.ai-row {
  justify-content: flex-start;
}

.chat-bubble {
  max-width: 78%;
  border-radius: 18px;
  padding: 14px 16px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
}

.user-bubble {
  background: rgba(106, 85, 255, 0.1);
  border: 1px solid rgba(106, 85, 255, 0.12);
}

.ai-bubble {
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.bubble-label {
  font-size: 12px;
  font-weight: 600;
  color: #6a55ff;
  margin-bottom: 6px;
}

.bubble-text {
  font-size: 15px;
  line-height: 1.7;
  color: #333;
  white-space: pre-wrap;
  word-break: break-word;
}

.bubble-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  font-size: 12px;
  color: #999;
}

.question-card,
.answer-card {
  border-radius: 20px;
  padding: 20px 22px;
  transition: all 0.3s ease;
}

.question-card {
  background: rgba(106, 85, 255, 0.05);
  border: 1px solid rgba(106, 85, 255, 0.1);
  align-self: flex-end;
  max-width: 85%;
  margin-left: auto;
  
  &:hover {
    background: rgba(106, 85, 255, 0.08);
  }
}

.answer-card {
  background: rgba(248, 247, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.05);
  align-self: flex-start;
  max-width: 85%;
  
  &:hover {
    background: white;
    border-color: rgba(106, 85, 255, 0.1);
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: #6a55ff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  flex-shrink: 0;
  
  &.ai {
    background: linear-gradient(135deg, #4ecdc4, #44a08d);
  }
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 2px;
}

.card-time {
  font-size: 12px;
  color: #999;
}

.card-content {
  font-size: 15px;
  line-height: 1.7;
  color: #333;
  white-space: pre-wrap;
  word-break: break-word;
  margin-bottom: 12px;
}

.card-footer {
  display: flex;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.small-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
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
  
  &.ghost {
    background: transparent;
    border-color: rgba(106, 85, 255, 0.18);
  }
  
  svg {
    stroke: currentColor;
  }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: rgba(248, 247, 255, 0.5);
  border-radius: 20px;
  border: 2px dashed rgba(106, 85, 255, 0.2);
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
  .ai-page {
    padding: 0 16px 24px;
  }
  
  .ai-header {
    padding: 24px;
    border-radius: 20px;
    flex-direction: column;
    gap: 24px;
    text-align: center;
    margin: 16px 0;
  }
  
  .header-content {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .header-icon {
    width: 64px;
    height: 64px;
    font-size: 32px;
    margin: 0 auto;
  }
  
  .header-text h1 {
    font-size: 24px;
  }
  
  .header-stats {
    justify-content: center;
    width: 100%;
  }
  
  .stat-item {
    padding: 16px 20px;
    min-width: 120px;
  }
  
  .ai-input-section {
    padding: 24px;
    border-radius: 20px;
  }
  
  .quick-buttons {
    width: 100%;
    overflow-x: auto;
    padding-bottom: 8px;
    
    &::-webkit-scrollbar {
      height: 4px;
    }
    
    &::-webkit-scrollbar-track {
      background: rgba(0, 0, 0, 0.05);
      border-radius: 2px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: rgba(106, 85, 255, 0.3);
      border-radius: 2px;
    }
  }
  
  .textarea-footer {
    flex-direction: column;
    align-items: stretch;
  }
  
  .submit-btn {
    width: 100%;
  }
  
  .history-section {
    padding: 24px;
    border-radius: 20px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .section-actions {
    align-self: flex-end;
  }
  
  .question-card,
  .answer-card {
    max-width: 100%;
  }
}
</style>