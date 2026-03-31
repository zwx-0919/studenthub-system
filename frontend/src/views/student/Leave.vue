<template>
  <div class="leave-request-page">
    <!-- 提交请假申请卡片 -->
    <div class="leave-card submit-card">
      <div class="card-header">
        <div class="header-icon">📋</div>
        <div class="header-content">
          <h3>请假申请</h3>
          <p>填写请假信息，提交请假申请</p>
        </div>
      </div>

      <div class="form-container">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">
              <svg class="label-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                <line x1="16" y1="2" x2="16" y2="6"></line>
                <line x1="8" y1="2" x2="8" y2="6"></line>
                <line x1="3" y1="10" x2="21" y2="10"></line>
              </svg>
              请假类型
            </label>
            <div class="select-wrapper">
              <select v-model.number="form.leaveType" class="form-select">
                <option :value="1">事假</option>
                <option :value="2">病假</option>
                <option :value="3">其他</option>
              </select>
              <div class="select-arrow">▼</div>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">
              <svg class="label-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              开始时间
            </label>
            <div class="datetime-wrapper">
              <input v-model="form.startTime" type="datetime-local" class="datetime-input" :min="minStartTime" />
            </div>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">
              <svg class="label-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              结束时间
            </label>
            <div class="datetime-wrapper">
              <input v-model="form.endTime" type="datetime-local" class="datetime-input" :min="form.startTime || minStartTime" />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">
              <svg class="label-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
                <line x1="16" y1="13" x2="8" y2="13"></line>
                <line x1="16" y1="17" x2="8" y2="17"></line>
                <polyline points="10 9 9 9 8 9"></polyline>
              </svg>
              请假理由
            </label>
            <textarea 
              v-model="form.reason" 
              class="form-textarea" 
              placeholder="请填写详细的请假理由(必填)" 
              rows="3"
              maxlength="200"
            />
            <div class="input-count">{{ form.reason.length }}/200</div>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">
            <svg class="label-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"></path>
              <polyline points="13 2 13 9 20 9"></polyline>
            </svg>
            证明文件（可选）
          </label>
          <div class="upload-area" @click="triggerFileInput">
            <input 
              ref="fileInput"
              type="file" 
              @change="pickFile"
              style="display: none"
              accept=".jpg,.jpeg,.png,.pdf,.doc,.docx"
            />
            <div class="upload-placeholder" v-if="!fileRef">
              <svg class="upload-icon" viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="17 8 12 3 7 8"></polyline>
                <line x1="12" y1="3" x2="12" y2="15"></line>
              </svg>
              <p>点击上传证明文件</p>
              <p class="upload-sub">支持图片、PDF、Word文档，不超过10MB</p>
            </div>
            <div v-else class="file-preview">
              <div class="preview-icon">
                <svg class="file-icon" viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"></path>
                  <polyline points="13 2 13 9 20 9"></polyline>
                </svg>
              </div>
              <div class="file-info">
                <div class="file-name">{{ fileRef.name }}</div>
                <div class="file-size">{{ formatFileSize(fileRef.size) }}</div>
              </div>
              <button class="remove-btn" @click.stop="clearFile">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            </div>
          </div>
        </div>

        <div v-if="msg" class="message-box" :class="messageType">
          <svg v-if="messageType === 'success'" class="message-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
            <polyline points="22 4 12 14.01 9 11.01"></polyline>
          </svg>
          <svg v-else class="message-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
          <span>{{ msg }}</span>
        </div>

        <button 
          class="submit-btn" 
          @click="submit"
          :disabled="!canSubmit || submitting"
          :class="{ 'disabled': !canSubmit || submitting }"
        >
          <svg v-if="!submitting" class="submit-icon" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
            <polyline points="14 2 14 8 20 8"></polyline>
            <line x1="16" y1="13" x2="8" y2="13"></line>
            <line x1="16" y1="17" x2="8" y2="17"></line>
            <polyline points="10 9 9 9 8 9"></polyline>
          </svg>
          <div v-else class="spinner"></div>
          {{ submitting ? '提交中...' : '提交申请' }}
        </button>
      </div>
    </div>

    <!-- 请假记录卡片 -->
    <div class="leave-card records-card">
      <div class="card-header">
        <div class="header-icon">📄</div>
        <div class="header-content">
          <div class="header-title">
            <h3>我的请假记录</h3>
            <div class="records-count">{{ list.length }} 条记录</div>
          </div>
          <p>查看您的请假申请历史</p>
        </div>
        <button class="refresh-btn" @click="load" title="刷新">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 4 23 10 17 10"></polyline>
            <polyline points="1 20 1 14 7 14"></polyline>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
          </svg>
        </button>
      </div>

      <div v-if="list.length > 0" class="records-list">
        <div v-for="item in list" :key="item.id" class="record-item">
          <div class="record-header">
            <div class="record-type" :class="getLeaveTypeClass(item.leaveType)">
              {{ getLeaveTypeText(item.leaveType) }}
            </div>
            <div class="record-status" :class="getStatusClass(item.approveStatus)">
              {{ statusText(item.approveStatus) }}
            </div>
          </div>
          
          <div class="record-content">
            <div class="record-reason">{{ item.reason }}</div>
            <div class="record-time">
              <div class="time-item">
                <svg class="time-icon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"></circle>
                  <polyline points="12 6 12 12 16 14"></polyline>
                </svg>
                开始：{{ formatDisplayTime(item.startTime) }}
              </div>
              <div class="time-item">
                <svg class="time-icon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"></circle>
                  <polyline points="12 6 12 12 16 14"></polyline>
                </svg>
                结束：{{ formatDisplayTime(item.endTime) }}
              </div>
            </div>
          </div>
          
          <div class="record-footer">
            <div class="record-id">#{{ item.id }}</div>
            <div class="record-date">申请：{{ formatTime(item.createTime) }}</div>
          </div>
        </div>
      </div>

      <div v-else class="empty-records">
        <div class="empty-icon">📋</div>
        <h3>暂无请假记录</h3>
        <p>您还没有提交过请假申请</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed, watch } from "vue";
import request from "@/utils/request";

const msg = ref("");
const fileInput = ref(null);
const fileRef = ref(null);
const submitting = ref(false);
const list = ref([]);
const form = reactive({ 
  leaveType: 1, 
  startTime: "", 
  endTime: "", 
  reason: "" 
});

// 计算属性
const canSubmit = computed(() => {
  return form.leaveType && 
         form.startTime && 
         form.endTime && 
         form.reason.trim().length >= 1 &&  
         form.reason.length <= 200 &&
         new Date(form.startTime) < new Date(form.endTime);
});

const messageType = computed(() => {
  return msg.value.includes("成功") ? "success" : "error";
});

// 获取当前时间作为最小可选时间
const minStartTime = computed(() => {
  const now = new Date();
  // 转换为 YYYY-MM-DDTHH:mm 格式
  return now.toISOString().slice(0, 16);
});

// 方法
const pickFile = (e) => {
  const file = e.target.files[0];
  if (file) {
    // 验证文件大小（10MB）
    if (file.size > 10 * 1024 * 1024) {
      msg.value = "文件大小不能超过10MB";
      return;
    }
    fileRef.value = file;
  }
};

const triggerFileInput = () => {
  fileInput.value?.click();
};

const clearFile = () => {
  fileRef.value = null;
  if (fileInput.value) {
    fileInput.value.value = "";
  }
};

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
};

// 格式化时间为 YYYY-MM-DD HH:mm
const formatDateTime = (v) => {
  if (!v) return "";
  return v.replace("T", " ").slice(0, 16);
};

const formatDisplayTime = (time) => {
  if (!time) return "";
  return time.replace("T", " ").slice(0, 16);
};

const formatTime = (time) => {
  if (!time) return "";
  const date = new Date(time);
  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const getLeaveTypeText = (type) => {
  const types = { 1: "事假", 2: "病假", 3: "其他" };
  return types[type] || "未知";
};

const getLeaveTypeClass = (type) => {
  const classes = { 1: "personal", 2: "sick", 3: "other" };
  return classes[type] || "other";
};

const getStatusClass = (status) => {
  const classes = { 0: "pending", 1: "approved", 2: "rejected" };
  return classes[status] || "pending";
};

const submit = async () => {
  if (!canSubmit.value || submitting.value) return;
  
  // 验证开始时间不晚于结束时间
  const start = new Date(form.startTime);
  const end = new Date(form.endTime);
  if (start >= end) {
    msg.value = "开始时间必须早于结束时间";
    return;
  }
  
  submitting.value = true;
  const fd = new FormData();
  fd.append("leaveType", form.leaveType);
  fd.append("startTime", formatDateTime(form.startTime));
  fd.append("endTime", formatDateTime(form.endTime));
  fd.append("reason", form.reason.trim());
  if (fileRef.value) fd.append("proofFile", fileRef.value);
  
  try {
    const res = await request.post("/api/student/leave/add", fd);
    msg.value = res.msg || "提交成功";
    
    // 清空表单
    form.leaveType = 1;
    form.startTime = "";
    form.endTime = "";
    form.reason = "";
    clearFile();
    
    // 重新加载列表
    await load();
    
    // 3秒后清空消息
    setTimeout(() => {
      msg.value = "";
    }, 3000);
  } catch (e) {
    msg.value = e.response?.data?.msg || e.message || "提交失败";
  } finally {
    submitting.value = false;
  }
};

const statusText = (v) => {
  if (v === 0) return "待审批";
  if (v === 1) return "已通过";
  if (v === 2) return "已驳回";
  return String(v ?? "");
};

const load = async () => {
  try {
    const res = await request.get("/api/student/leave/page", { 
      params: { current: 1, size: 20 } 
    });
    list.value = res.data?.records || [];
  } catch (error) {
    console.error("加载失败:", error);
  }
};

onMounted(() => {
  load();
  // 设置默认开始时间为1小时后，结束时间为2小时后
  const now = new Date();
  const startTime = new Date(now.getTime() + 60 * 60 * 1000);
  const endTime = new Date(startTime.getTime() + 60 * 60 * 1000);
  
  form.startTime = startTime.toISOString().slice(0, 16);
  form.endTime = endTime.toISOString().slice(0, 16);
});

// 监听理由长度
watch(() => form.reason, (reason) => {
  if (reason.length > 200) {
    form.reason = reason.substring(0, 200);
  }
});

// 监听开始时间变化，自动调整结束时间
watch(() => form.startTime, (newStartTime) => {
  if (newStartTime && form.endTime) {
    const start = new Date(newStartTime);
    const end = new Date(form.endTime);
    if (end <= start) {
      // 如果结束时间不晚于开始时间，自动设置为开始时间后1小时
      const newEnd = new Date(start.getTime() + 60 * 60 * 1000);
      form.endTime = newEnd.toISOString().slice(0, 16);
    }
  }
});
</script>

<style scoped lang="less">
.leave-request-page {
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
.leave-card {
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

.submit-card {
  .card-header {
    margin-bottom: 24px;
  }
}

.records-card {
  .card-header {
    margin-bottom: 20px;
  }
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: flex-start;
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

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.records-count {
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 10px;
  background: rgba(106, 85, 255, 0.1);
  color: #6a55ff;
  font-weight: 500;
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

/* 表单样式 */
.form-container {
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
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
  margin-bottom: 8px;
  
  .label-icon {
    stroke: #666;
  }
}

/* 下拉框样式 */
.select-wrapper {
  position: relative;
  width: 100%;
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
  padding-right: 40px;
  
  &:focus {
    outline: none;
    border-color: #6a55ff;
    box-shadow: 0 0 0 3px rgba(106, 85, 255, 0.1);
    background: white;
  }
}

.select-arrow {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
  pointer-events: none;
  font-size: 12px;
}

/* 日期时间输入框 */
.datetime-wrapper {
  position: relative;
  width: 100%;
}

.datetime-input {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 0 16px;
  font-size: 15px;
  transition: all 0.3s ease;
  background: rgba(248, 247, 255, 0.5);
  padding-right: 40px;
  
  &:focus {
    outline: none;
    border-color: #6a55ff;
    box-shadow: 0 0 0 3px rgba(106, 85, 255, 0.1);
    background: white;
  }
  
  &::-webkit-calendar-picker-indicator {
    position: absolute;
    right: 16px;
    top: 50%;
    transform: translateY(-50%);
    cursor: pointer;
    opacity: 0.5;
  }
}

.form-textarea {
  width: 100%;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 12px 16px;
  font-size: 15px;
  font-family: inherit;
  transition: all 0.3s ease;
  background: rgba(248, 247, 255, 0.5);
  resize: vertical;
  min-height: 100px;
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

.input-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 上传区域 */
.upload-area {
  border: 2px dashed rgba(106, 85, 255, 0.3);
  border-radius: 12px;
  padding: 20px;
  background: rgba(248, 247, 255, 0.3);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(106, 85, 255, 0.05);
    border-color: rgba(106, 85, 255, 0.5);
  }
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #666;
  text-align: center;
}

.upload-icon {
  stroke-width: 1.5;
  margin-bottom: 4px;
}

.upload-sub {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.file-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.preview-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(106, 85, 255, 0.1), rgba(138, 122, 255, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-icon {
  stroke: #6a55ff;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 12px;
  color: #999;
}

.remove-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: white;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  
  &:hover {
    background: rgba(255, 107, 107, 0.1);
    color: #ff6b6b;
    border-color: rgba(255, 107, 107, 0.2);
  }
  
  svg {
    stroke: currentColor;
  }
}

/* 消息提示 */
.message-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  
  &.success {
    background: rgba(78, 205, 196, 0.1);
    border: 1px solid rgba(78, 205, 196, 0.2);
    color: #4ecdc4;
    
    .message-icon {
      stroke: #4ecdc4;
    }
  }
  
  &.error {
    background: rgba(255, 107, 107, 0.1);
    border: 1px solid rgba(255, 107, 107, 0.2);
    color: #ff6b6b;
    
    .message-icon {
      stroke: #ff6b6b;
    }
  }
}

.message-icon {
  flex-shrink: 0;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  margin-top: 8px;
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
  
  .submit-icon {
    stroke: white;
  }
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 记录列表 */
.records-list {
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

.record-item {
  background: rgba(248, 247, 255, 0.5);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid rgba(106, 85, 255, 0.1);
  transition: all 0.3s ease;
  
  &:hover {
    background: white;
    border-color: rgba(106, 85, 255, 0.2);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(106, 85, 255, 0.1);
  }
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}

.record-type {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 8px;
  font-weight: 500;
  
  &.personal {
    background: rgba(78, 205, 196, 0.1);
    color: #4ecdc4;
    border: 1px solid rgba(78, 205, 196, 0.2);
  }
  
  &.sick {
    background: rgba(255, 107, 107, 0.1);
    color: #ff6b6b;
    border: 1px solid rgba(255, 107, 107, 0.2);
  }
  
  &.other {
    background: rgba(255, 230, 109, 0.1);
    color: #d4a017;
    border: 1px solid rgba(255, 230, 109, 0.2);
  }
}

.record-status {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 8px;
  font-weight: 500;
  
  &.pending {
    background: rgba(255, 230, 109, 0.1);
    color: #d4a017;
    border: 1px solid rgba(255, 230, 109, 0.2);
  }
  
  &.approved {
    background: rgba(78, 205, 196, 0.1);
    color: #4ecdc4;
    border: 1px solid rgba(78, 205, 196, 0.2);
  }
  
  &.rejected {
    background: rgba(255, 107, 107, 0.1);
    color: #ff6b6b;
    border: 1px solid rgba(255, 107, 107, 0.2);
  }
}

.record-content {
  margin-bottom: 12px;
}

.record-reason {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 12px;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.record-time {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #666;
  
  .time-icon {
    stroke: #999;
  }
}

.record-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  font-size: 12px;
  color: #999;
}

.record-date {
  opacity: 0.8;
}

/* 空状态 */
.empty-records {
  text-align: center;
  padding: 40px 20px;
  background: rgba(248, 247, 255, 0.5);
  border-radius: 12px;
  border: 2px dashed rgba(106, 85, 255, 0.2);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-records h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.empty-records p {
  color: #666;
  margin: 0;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .leave-card {
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
  
  .form-select,
  .datetime-input {
    height: 44px;
    font-size: 14px;
  }
  
  .submit-btn {
    height: 48px;
    font-size: 15px;
  }
  
  .records-list {
    max-height: 400px;
  }
}
</style>