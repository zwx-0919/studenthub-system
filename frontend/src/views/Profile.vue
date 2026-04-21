<template>
  <div class="profile-page">
    <!-- 个人资料卡片 -->
    <div class="profile-card">
      <!-- 卡片头部 -->
      <div class="card-header">
        <div class="header-icon">👤</div>
        <div class="header-content">
          <h3>个人信息</h3>
          <p>管理您的个人资料和账户设置</p>
        </div>
      </div>

      <!-- 头像区域 -->
      <div class="avatar-section">
        <div class="avatar-container">
          <div v-if="form.userAvatar" class="avatar-img-wrapper">
            <img :src="form.userAvatar" class="avatar-img" alt="用户头像" />
          </div>
          <div v-else class="avatar-circle">
            {{ (form.userName || "用").slice(0, 1) }}
          </div>
          
          <div class="avatar-actions">
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="onAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              class="avatar-upload"
            >
              <button class="upload-btn">
                <svg class="upload-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="17 8 12 3 7 8"></polyline>
                  <line x1="12" y1="3" x2="12" y2="15"></line>
                </svg>
                上传头像
              </button>
            </el-upload>
            <div class="upload-hint">支持 JPG/PNG 格式，不超过 2MB</div>
          </div>
        </div>
      </div>

      <!-- 个人信息表单 -->
      <div class="profile-form">
        <div class="form-section">
          <h4 class="section-title">📄 基本信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <div class="info-label">
                <svg class="info-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
                姓名
              </div>
              <div class="info-value">{{ form.userName || "-" }}</div>
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
              <div class="info-value">{{ formatTime(form.createTime) || "-" }}</div>
            </div>

            <div class="info-item">
              <div class="info-label">
                <svg class="info-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                  <line x1="16" y1="2" x2="16" y2="6"></line>
                  <line x1="8" y1="2" x2="8" y2="6"></line>
                  <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                上次登录
              </div>
              <div class="info-value">{{ formatTime(form.lastLoginTime) || "-" }}</div>
            </div>

            <div class="info-item">
              <div class="info-label">
                <svg class="info-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                  <polyline points="22,6 12,13 2,6"></polyline>
                </svg>
                邮箱
              </div>
              <div class="email-input">
                <input 
                  v-model="form.email" 
                  placeholder="请输入邮箱地址" 
                  class="email-field"
                />
                <button 
                  v-if="emailChanged" 
                  class="email-save-btn"
                  @click="saveEmail"
                  title="保存邮箱"
                >
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 6L9 17l-5-5"></path>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 角色特定信息 -->
        <div v-if="store.role === 1" class="form-section">
          <h4 class="section-title">🎓 学生信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <div class="info-label">
                <svg class="info-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
                  <path d="M2 17l10 5 10-5"></path>
                  <path d="M2 12l10 5 10-5"></path>
                </svg>
                学号
              </div>
              <div class="info-value">{{ form.userAccount || "-" }}</div>
            </div>

            <!-- 修改这里：从注册时间改为班级信息 -->
            <div class="info-item">
              <div class="info-label">
                <svg class="info-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                  <line x1="16" y1="2" x2="16" y2="6"></line>
                  <line x1="8" y1="2" x2="8" y2="6"></line>
                  <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                班级
              </div>
              <div class="info-value">
                {{ getClassInfo() }}
              </div>
            </div>

            <div v-if="form.counselorName" class="info-item">
              <div class="info-label">
                <svg class="info-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
                辅导员
              </div>
              <div class="info-value">{{ form.counselorName }}</div>
            </div>
          </div>
        </div>

        <div v-if="store.role === 2" class="form-section">
          <h4 class="section-title">👨‍🏫 辅导员信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <div class="info-label">
                <svg class="info-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
                  <path d="M2 17l10 5 10-5"></path>
                  <path d="M2 12l10 5 10-5"></path>
                </svg>
                工号
              </div>
              <div class="info-value">{{ form.userAccount || "-" }}</div>
            </div>

            <div v-if="form.chargeScopeText || form.managedMajors || form.managedClasses" class="info-item full-width">
              <div class="info-label">
                <svg class="info-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                  <line x1="16" y1="2" x2="16" y2="6"></line>
                  <line x1="8" y1="2" x2="8" y2="6"></line>
                  <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                负责范围
              </div>
              <div class="info-value">
                {{ getResponsibleScope() }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 密码修改区域 -->
      <div class="password-section">
        <h4 class="section-title">🔐 修改密码</h4>
        <div class="password-form">
          <div class="password-inputs">
            <div class="password-group">
              <label class="password-label">旧密码</label>
              <input 
                v-model="pwd.oldPassword" 
                type="password" 
                placeholder="请输入当前密码" 
                class="password-field"
              />
            </div>
            <div class="password-group">
              <label class="password-label">新密码</label>
              <input 
                v-model="pwd.newPassword" 
                type="password" 
                placeholder="请输入新密码" 
                class="password-field"
              />
            </div>
          </div>
          <button 
            class="change-pwd-btn" 
            @click="changePwd"
            :disabled="!canChangePassword"
            :class="{ 'disabled': !canChangePassword }"
          >
            修改密码
          </button>
        </div>
      </div>
    </div>
  </div>

</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/store/user";
import request from "@/utils/request";

const store = useUserStore();
const form = reactive({
  userName: "",
  email: "",
  userAccount: "",
  lastLoginTime: "",
  createTime: "",
  classNo: "",
  className: "", // 新增班级名称字段
  college: "",   // 学院信息
  major: "",     // 专业信息
  counselorName: "",
  managedClasses: "",
  managedMajors: "",
  chargeScopeText: "",
  userAvatar: ""
});
const pwd = reactive({ oldPassword: "", newPassword: "" });
const originalEmail = ref("");

// 计算属性
const emailChanged = computed(() => {
  return form.email !== originalEmail.value;
});

const canChangePassword = computed(() => {
  return pwd.oldPassword.trim() && pwd.newPassword.trim();
});

// 上传配置
const uploadUrl = "/user/avatar/upload";
const uploadHeaders = computed(() => {
  const uid = store.user?.id || localStorage.getItem("active_user_id");
  const token = uid ? localStorage.getItem(`user_${uid}_token`) : store.token;
  return { Authorization: token ? `Bearer ${token}` : "" };
});

// 方法
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

// 获取班级信息
const getClassInfo = () => {
  if (form.className) {
    return form.className;
  } else if (form.classNo) {
    return form.classNo;
  } else if (form.college || form.major) {
    return `${form.college || ''}${form.major ? ` - ${form.major}` : ''}`;
  }
  return "-";
};

// 获取负责范围
const getResponsibleScope = () => {
  if (form.chargeScopeText && form.chargeScopeText !== "-") {
    return form.chargeScopeText;
  }
  if (form.managedMajors && form.managedClasses) {
    return `${form.managedMajors} (${form.managedClasses})`;
  } else if (form.managedMajors) {
    return form.managedMajors;
  } else if (form.managedClasses) {
    return form.managedClasses;
  }
  return "-";
};

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith("image/");
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isImage) {
    ElMessage.error("只能上传图片文件");
    return false;
  }
  if (!isLt2M) {
    ElMessage.error("图片大小不能超过 2MB");
    return false;
  }
  return true;
};

const onAvatarSuccess = async (res) => {
  if (res?.code === 1 && res?.data?.url) {
    form.userAvatar = res.data.url;
    try {
      await request.put("/user/profile", { userAvatar: form.userAvatar });
      ElMessage.success("头像更新成功");
    } catch (error) {
      ElMessage.error(error?.message || "头像保存失败");
    }
  } else {
    ElMessage.error(res?.msg || "上传失败");
  }
};

const load = async () => {
  try {
    const res = await request.get("/user/profile");
    Object.assign(form, res.data || {});
    originalEmail.value = form.email;
  } catch (error) {
    console.error("加载个人信息失败:", error);
  }
};

const saveEmail = async () => {
  try {
    const res = await request.put("/user/profile", { 
      email: form.email, 
      userAvatar: form.userAvatar 
    });
    originalEmail.value = form.email;
    ElMessage.success(res?.msg || "邮箱保存成功");
  } catch (e) {
    ElMessage.error(e?.msg || "保存失败");
  }
};

const saveProfile = async () => {
  await saveEmail();
};

const changePwd = async () => {
  if (!pwd.oldPassword || !pwd.newPassword) return;
  try {
    const res = await request.put("/user/password", pwd);
    ElMessage.success(res?.msg || "修改密码成功");
    pwd.oldPassword = "";
    pwd.newPassword = "";
  } catch (e) {
    ElMessage.error(e?.msg || "修改失败");
  }
};

onMounted(load);

// 监听邮箱输入
watch(() => form.email, (newEmail) => {
  if (newEmail !== originalEmail.value) {
    // 邮箱已修改
  }
});
</script>

<style scoped lang="less">
.profile-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px 40px;
  background: #f8f9ff;
  min-height: 100vh;
}

/* 卡片样式 */
.profile-card {
  background: white;
  border-radius: 20px;
  padding: 28px;
  margin-top: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
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

/* 头像区域 */
.avatar-section {
  margin-bottom: 32px;
}

.avatar-container {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px;
  background: rgba(248, 247, 255, 0.5);
  border-radius: 16px;
  border: 1px solid rgba(106, 85, 255, 0.1);
  
  @media (max-width: 768px) {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
}

.avatar-img-wrapper {
  width: 100px;
  height: 100px;
  border-radius: 20px;
  overflow: hidden;
  border: 4px solid white;
  box-shadow: 0 8px 32px rgba(106, 85, 255, 0.2);
  background: linear-gradient(135deg, rgba(106, 85, 255, 0.1), rgba(138, 122, 255, 0.1));
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-circle {
  width: 100px;
  height: 100px;
  border-radius: 20px;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-weight: 700;
  border: 4px solid white;
  box-shadow: 0 8px 32px rgba(106, 85, 255, 0.2);
}

.avatar-actions {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: white;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  width: fit-content;
  
  &:hover {
    background: rgba(106, 85, 255, 0.08);
    color: #6a55ff;
    border-color: rgba(106, 85, 255, 0.2);
  }
  
  .upload-icon {
    stroke: currentColor;
  }
}

.avatar-upload {
  display: inline-block;
}

.upload-hint {
  font-size: 12px;
  color: #999;
  margin: 0;
}

/* 表单区域 */
.profile-form {
  margin-bottom: 32px;
}

.form-section {
  margin-bottom: 28px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
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
  
  &.full-width {
    grid-column: 1 / -1;
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
  min-height: 24px;
  display: flex;
  align-items: center;
}

.email-input {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.email-field {
  flex: 1;
  height: 40px;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 0 12px;
  font-size: 15px;
  transition: all 0.3s ease;
  background: white;
  
  &:focus {
    outline: none;
    border-color: #6a55ff;
    box-shadow: 0 0 0 3px rgba(106, 85, 255, 0.1);
  }
  
  &::placeholder {
    color: #999;
  }
}

.email-save-btn {
  width: 40px;
  height: 40px;
  border-radius: 8px;
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

/* 密码修改区域 */
.password-section {
  padding: 20px;
  background: rgba(248, 247, 255, 0.5);
  border-radius: 16px;
  border: 1px solid rgba(106, 85, 255, 0.1);
}

.password-form {
  display: flex;
  align-items: center;
  gap: 20px;
  
  @media (max-width: 768px) {
    flex-direction: column;
    align-items: stretch;
  }
}

.password-inputs {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.password-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.password-label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.password-field {
  height: 44px;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 0 12px;
  font-size: 15px;
  transition: all 0.3s ease;
  background: white;
  
  &:focus {
    outline: none;
    border-color: #6a55ff;
    box-shadow: 0 0 0 3px rgba(106, 85, 255, 0.1);
  }
  
  &::placeholder {
    color: #999;
  }
}

.change-pwd-btn {
  padding: 12px 28px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
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
  
  @media (max-width: 768px) {
    width: 100%;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-page {
    padding: 0 16px 24px;
  }
  
  .profile-card {
    padding: 20px;
    border-radius: 16px;
    margin-top: 16px;
  }
  
  .header-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .header-content h3 {
    font-size: 18px;
  }
  
  .avatar-circle {
    width: 80px;
    height: 80px;
    font-size: 32px;
  }
  
  .avatar-img-wrapper {
    width: 80px;
    height: 80px;
  }
}
</style>