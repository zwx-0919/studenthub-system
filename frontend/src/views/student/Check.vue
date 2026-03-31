<template>
  <div class="checkin-page">
    <!-- 提交打卡卡片 -->
    <div class="checkin-card submit-card">
      <div class="card-header">
        <div class="header-icon">📍</div>
        <div class="header-content">
          <h3>提交打卡</h3>
          <p>记录今日学习进度，分享学习生活</p>
        </div>
      </div>

      <div class="form-container">
        <div class="form-group">
          <label class="form-label">
            <svg class="label-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
              <path d="M2 17l10 5 10-5"></path>
              <path d="M2 12l10 5 10-5"></path>
            </svg>
            打卡内容
          </label>
          <input 
            v-model="form.content" 
            class="form-input" 
            placeholder="例如：今天完成了高等数学第3章的学习" 
            maxlength="100"
          />
          <div class="input-count">{{ form.content.length }}/100</div>
        </div>

        <div class="form-group">
          <label class="form-label">
            <svg class="label-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
              <circle cx="12" cy="10" r="3"></circle>
            </svg>
            具体位置
          </label>
          <input 
            v-model="form.location" 
            class="form-input" 
            placeholder="例如：图书馆三楼自习区、教学楼302室" 
            maxlength="50"
          />
          <div class="input-count">{{ form.location.length }}/50</div>
        </div>

        <div class="form-group">
          <label class="form-label">
            <svg class="label-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <circle cx="8.5" cy="8.5" r="1.5"></circle>
              <polyline points="21 15 16 10 5 21"></polyline>
            </svg>
            上传图片（可选）
          </label>
          <div class="upload-area" @click="triggerFileInput">
            <input 
              ref="fileInput"
              type="file" 
              accept="image/*" 
              @change="pickFile"
              style="display: none"
            />
            <div class="upload-placeholder" v-if="!fileRef">
              <svg class="upload-icon" viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="17 8 12 3 7 8"></polyline>
                <line x1="12" y1="3" x2="12" y2="15"></line>
              </svg>
              <p>点击选择图片</p>
              <p class="upload-sub">支持 JPG/PNG 格式，不超过 5MB</p>
            </div>
            <div v-else class="file-preview">
              <div class="preview-image">
                <img v-if="previewUrl" :src="previewUrl" alt="预览" />
                <div v-else class="preview-placeholder">
                  <svg class="preview-icon" viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"></path>
                    <polyline points="13 2 13 9 20 9"></polyline>
                  </svg>
                </div>
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

        <button 
          class="submit-btn" 
          @click="submit"
          :disabled="!canSubmit"
          :class="{ 'disabled': !canSubmit }"
        >
          <svg v-if="!submitting" class="submit-icon" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
            <polyline points="22 4 12 14.01 9 11.01"></polyline>
          </svg>
          <div v-else class="spinner"></div>
          {{ submitting ? '提交中...' : '提交打卡' }}
        </button>
      </div>
    </div>

    <!-- 打卡记录卡片 -->
    <div class="checkin-card records-card">
      <div class="card-header">
        <div class="header-icon">📅</div>
        <div class="header-content">
          <div class="header-title">
            <h3>我的打卡记录</h3>
            <div class="records-count">{{ list.length }} 条记录</div>
          </div>
          <p>查看近期的学习打卡情况</p>
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
            <div class="record-time">{{ formatTime(item.checkTime) }}</div>
            <div class="record-location" v-if="item.location">
              <svg class="location-icon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                <circle cx="12" cy="10" r="3"></circle>
              </svg>
              {{ item.location }}
            </div>
          </div>
          <div class="record-content">{{ item.content }}</div>
          <div v-if="item.imageUrl" class="record-image">
            <el-image
              class="record-img"
              :src="item.imageUrl"
              fit="cover"
              preview-teleported
              :preview-src-list="[item.imageUrl]"
            />
          </div>
          <div class="record-footer">
            <div class="record-id">#{{ item.id }}</div>
          </div>
        </div>
      </div>

      <div v-else class="empty-records">
        <div class="empty-icon">📝</div>
        <h3>暂无打卡记录</h3>
        <p>开始您的第一次学习打卡吧！</p>
      </div>
    </div>

    <!-- 成功提示 -->
    <div v-if="successVisible" class="success-mask">
      <div class="success-box">
        <div class="success-icon">✓</div>
        <h3>打卡成功！</h3>
        <p>您已成功提交学习打卡</p>
        <div class="success-progress">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: `${progress}%` }"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed, watch } from "vue";
import request from "@/utils/request";

const list = ref([]);
const fileInput = ref(null);
const fileRef = ref(null);
const previewUrl = ref("");
const successVisible = ref(false);
const submitting = ref(false);
const progress = ref(100);
const form = reactive({ content: "", location: "" });

// 计算属性
const canSubmit = computed(() => {
  return form.content.trim().length >= 5 && 
         form.content.length <= 100 &&
         form.location.trim().length >= 2 &&
         form.location.length <= 50;
});

// 方法
const pickFile = (e) => {
  const file = e.target.files[0];
  if (file) {
    // 验证文件类型
    if (!file.type.match('image.*')) {
      alert('请选择图片文件');
      return;
    }
    
    // 验证文件大小（5MB）
    if (file.size > 5 * 1024 * 1024) {
      alert('图片大小不能超过5MB');
      return;
    }
    
    fileRef.value = file;
    
    // 创建预览URL
    const reader = new FileReader();
    reader.onload = (e) => {
      previewUrl.value = e.target.result;
    };
    reader.readAsDataURL(file);
  }
};

const triggerFileInput = () => {
  fileInput.value?.click();
};

const clearFile = () => {
  fileRef.value = null;
  previewUrl.value = "";
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

const formatTime = (time) => {
  if (!time) return "";
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

const submit = async () => {
  if (!canSubmit.value || submitting.value) return;
  
  submitting.value = true;
  const fd = new FormData();
  fd.append("content", form.content.trim());
  fd.append("location", form.location.trim());
  if (fileRef.value) fd.append("image", fileRef.value);
  
  try {
    await request.post("/api/student/check/add", fd);
    
    // 显示成功提示
    successVisible.value = true;
    progress.value = 100;
    
    // 3秒后关闭提示
    const interval = setInterval(() => {
      progress.value -= 100 / 30; // 3秒内从100%到0%
      if (progress.value <= 0) {
        clearInterval(interval);
        successVisible.value = false;
        progress.value = 100;
      }
    }, 100);
    
    // 清空表单
    form.content = "";
    form.location = "";
    clearFile();
    
    // 重新加载列表
    await load();
  } catch (error) {
    console.error("提交失败:", error);
  } finally {
    submitting.value = false;
  }
};

const load = async () => {
  try {
    const res = await request.get("/api/student/check/page", { 
      params: { current: 1, size: 20 } 
    });
    list.value = res.data?.records || [];
  } catch (error) {
    console.error("加载失败:", error);
  }
};

onMounted(load);

// 监听表单内容长度
watch(() => form.content, (content) => {
  if (content.length > 100) {
    form.content = content.substring(0, 100);
  }
});

watch(() => form.location, (location) => {
  if (location.length > 50) {
    form.location = location.substring(0, 50);
  }
});
</script>

<style scoped lang="less">
.checkin-page {
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
.checkin-card {
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

.form-input {
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

.preview-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(106, 85, 255, 0.1), rgba(138, 122, 255, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.preview-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-icon {
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

.record-time {
  font-size: 13px;
  font-weight: 500;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}

.record-location {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(106, 85, 255, 0.08);
  padding: 4px 8px;
  border-radius: 8px;
  
  .location-icon {
    stroke: #666;
  }
}

.record-content {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 12px;
  word-break: break-word;
}

.record-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: scale(1.05);
  }
}

.record-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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

.record-id {
  opacity: 0.7;
}

.record-status {
  padding: 4px 8px;
  border-radius: 8px;
  font-weight: 500;
  
  &.verified {
    background: rgba(78, 205, 196, 0.1);
    color: #4ecdc4;
  }
  
  &:not(.verified) {
    background: rgba(255, 230, 109, 0.1);
    color: #d4a017;
  }
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

/* 成功提示 */
.success-mask {
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

.success-box {
  background: white;
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  max-width: 320px;
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

.success-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4ecdc4, #44a08d);
  color: white;
  font-size: 32px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  box-shadow: 0 8px 20px rgba(78, 205, 196, 0.3);
}

.success-box h3 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.success-box p {
  color: #666;
  margin: 0 0 20px;
  line-height: 1.6;
}

.success-progress {
  width: 100%;
  height: 4px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.progress-bar {
  width: 100%;
  height: 100%;
  background: rgba(106, 85, 255, 0.1);
  border-radius: 2px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #6a55ff, #8a7aff);
  border-radius: 2px;
  transition: width 0.1s linear;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .checkin-card {
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
  
  .form-input {
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
  
  .success-box {
    padding: 30px 20px;
  }
}
</style>