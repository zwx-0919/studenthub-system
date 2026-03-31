<template>
  <div class="user-management-page">
    <!-- 用户管理卡片 -->
    <div class="management-card">
      <div class="card-header">
        <div class="header-icon">👥</div>
        <div class="header-content">
          <h3>用户管理</h3>
          <p>管理系统用户信息和权限</p>
        </div>
        <div class="card-actions">
          <button class="action-btn add-btn" @click="openAdd">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
            新增用户
          </button>
        </div>
      </div>

      <!-- 过滤工具栏 -->
      <div class="filter-toolbar">
        <div class="filter-group">
          <div class="filter-label">筛选条件</div>
          <div class="filter-actions">
            <el-select
              v-model="query.role"
              placeholder="身份筛选"
              clearable
              class="filter-select"
            >
              <el-option label="学生" :value="1" />
              <el-option label="辅导员" :value="2" />
            </el-select>
            <el-select
              v-model="query.classId"
              placeholder="班级筛选"
              clearable
              class="filter-select"
            >
              <el-option v-for="c in classOptions" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
            <el-input
              v-model="query.userAccount"
              placeholder="学号/工号"
              clearable
              class="search-input"
            />
            <el-input
              v-model="query.userName"
              placeholder="姓名"
              clearable
              class="search-input"
            />
            <button class="search-btn" @click="load">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
              查询
            </button>
          </div>
        </div>
      </div>

      <!-- 用户表格 -->
      <div class="table-container">
        <el-table
          :data="rows"
          style="width: 100%"
          row-class-name="table-row"
          header-row-class-name="table-header"
        >
          <el-table-column prop="id" label="ID" width="80">
            <template #default="{ row }">
              <div class="id-cell">#{{ row.id }}</div>
            </template>
          </el-table-column>
          
          <el-table-column prop="userAccount" label="学号/工号" width="120">
            <template #default="{ row }">
              <div class="account-cell">
                <div class="account">{{ row.userAccount }}</div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="userName" label="姓名" width="100">
            <template #default="{ row }">
              <div class="name-cell">
                <div class="name">{{ row.userName }}</div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="班级" width="120">
            <template #default="{ row }">
              <div class="class-cell">{{ classNameById(row.classId) }}</div>
            </template>
          </el-table-column>
          
          <el-table-column label="角色" width="100">
            <template #default="{ row }">
              <div class="role-badge" :class="{ 
                'student': row.role === 1, 
                'counselor': row.role === 2,
                'admin': row.role === 3
              }">
                {{ roleText(row.role) }}
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <div class="status-badge" :class="{ 'active': row.status === 1, 'muted': row.status !== 1 }">
                {{ row.status === 1 ? "正常" : "禁言" }}
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="240" align="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <button
                  class="action-btn status-btn"
                  :class="{ 'mute': row.status === 1, 'unmute': row.status !== 1 }"
                  @click="toggleStatus(row)"
                  :title="row.status === 1 ? '禁言用户' : '解除禁言'"
                >
                  <svg v-if="row.status === 1" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 1l22 22"></path>
                    <path d="M9 9v3a3 3 0 0 0 5.12 2.12M15 9.34V4a3 3 0 0 0-5.94-.6"></path>
                    <path d="M17 16.95A7 7 0 0 1 5 12v-2m14 0v-2a7 7 0 0 0-.11-1.23"></path>
                    <path d="M12 19v4"></path>
                    <path d="M8 23h8"></path>
                  </svg>
                  <svg v-else viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"></path>
                    <path d="M19 10v2a7 7 0 0 1-14 0v-2"></path>
                    <line x1="12" y1="19" x2="12" y2="23"></line>
                    <line x1="8" y1="23" x2="16" y2="23"></line>
                  </svg>
                  {{ row.status === 1 ? "禁言" : "解除" }}
                </button>
                <button class="action-btn delete-btn" @click="del(row)" title="删除用户">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                    <line x1="10" y1="11" x2="10" y2="17"></line>
                    <line x1="14" y1="11" x2="14" y2="17"></line>
                  </svg>
                  删除
                </button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="load"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- 新增用户弹窗 -->
    <el-dialog
      v-model="addDialogVisible"
      title="新增用户"
      width="600px"
      class="form-dialog"
    >
      <div class="form-content">
        <div class="form-grid">
          <div class="form-group">
            <label class="form-label">身份类型</label>
            <el-select
              v-model="addForm.identityType"
              placeholder="请选择身份类型"
              class="form-input"
              @change="handleIdentityTypeChange"
            >
              <el-option label="学生" :value="1" />
              <el-option label="辅导员" :value="2" />
              <el-option label="管理员" :value="3" />
            </el-select>
          </div>
          
          <div class="form-group">
            <label class="form-label">学号/工号</label>
            <el-input
              v-model="addForm.userAccount"
              placeholder="请输入学号或工号"
              class="form-input"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">姓名</label>
            <el-input
              v-model="addForm.userName"
              placeholder="请输入姓名"
              class="form-input"
            />
          </div>
          
          <div v-if="addForm.identityType === 1" class="form-group">
            <label class="form-label">班级</label>
            <el-select
              v-model="addForm.classId"
              placeholder="请选择班级"
              class="form-input"
            >
              <el-option v-for="c in classOptions" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </div>
          
          <div v-if="addForm.identityType === 2" class="form-group full-width">
            <label class="form-label">负责专业</label>
            <el-select
              v-model="chargeMajorIdList"
              multiple
              placeholder="请选择负责的专业"
              class="form-input"
            >
              <el-option v-for="m in majorOptions" :key="m.id" :label="m.name" :value="m.id" />
            </el-select>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <button class="dialog-btn cancel-btn" @click="addDialogVisible = false">取消</button>
          <button class="dialog-btn submit-btn" @click="submitAdd">创建</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import request from "@/utils/request";

const rows = ref([]);
const total = ref(0);
const classOptions = ref([]);
const majorOptions = ref([]);
const addDialogVisible = ref(false);
const chargeMajorIdList = ref([]);
const addForm = reactive({
  userAccount: "",
  userName: "",
  identityType: 1,
  classId: undefined
});
const query = reactive({
  current: 1,
  size: 10,
  role: undefined,
  classId: undefined,
  userAccount: "",
  userName: ""
});

const roleText = (r) => (r === 1 ? "学生" : r === 2 ? "辅导员" : r === 3 ? "管理员" : String(r ?? ""));
const classNameById = (id) => classOptions.value.find((c) => c.id === id)?.name || "-";

const load = async () => {
  const res = await request.get("/admin/user", {
    params: {
      current: query.current,
      size: query.size,
      role: query.role,
      classId: query.classId || undefined,
      userAccount: query.userAccount || undefined,
      userName: query.userName || undefined
    }
  });
  rows.value = res.data?.records || [];
  total.value = res.data?.total || 0;
};

const handleSizeChange = (size) => {
  query.size = size;
  query.current = 1;
  load();
};

const loadClasses = async () => {
  const res = await request.get("/api/admin/base/classes");
  classOptions.value = res.data || [];
};

const loadMajors = async () => {
  const res = await request.get("/api/admin/base/majors");
  majorOptions.value = res.data || [];
};

const openAdd = () => {
  addDialogVisible.value = true;
  addForm.userAccount = "";
  addForm.userName = "";
  addForm.identityType = 1;
  addForm.classId = undefined;
  chargeMajorIdList.value = [];
};

const handleIdentityTypeChange = () => {
  // 切换身份类型时清空相关字段
  addForm.classId = undefined;
  chargeMajorIdList.value = [];
};

const submitAdd = async () => {
  if (!addForm.userAccount?.trim()) {
    ElMessage.warning("请输入学号/工号");
    return;
  }
  if (!addForm.userName?.trim()) {
    ElMessage.warning("请输入姓名");
    return;
  }
  if (addForm.identityType === 1 && !addForm.classId) {
    ElMessage.warning("请选择班级");
    return;
  }
  if (addForm.identityType === 2 && chargeMajorIdList.value.length === 0) {
    ElMessage.warning("请选择负责的专业");
    return;
  }
  
  await request.post("/admin/user/add", {
    ...addForm,
    chargeMajorIds: chargeMajorIdList.value.join(",")
  });
  ElMessage.success("创建成功");
  addDialogVisible.value = false;
  load();
};

const toggleStatus = async (row) => {
  await request.patch("/admin/user/status", {
    id: row.id,
    status: row.status
  });
  ElMessage.success("操作成功");
  load();
};

const del = async (row) => {
  await ElMessageBox.confirm("确定删除该用户吗？", "提示", { type: "warning" });
  await request.delete("/admin/user", { data: [row.id] });
  ElMessage.success("删除成功");
  load();
};

onMounted(() => {
  load();
  loadClasses();
  loadMajors();
});
</script>

<style scoped lang="less">
.user-management-page {
  width: 100%;
  padding: 0;
  background: #f8f9ff;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  flex: 1;
}

/* 卡片样式 */
.management-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: fit-content;
  width: 100%;
  min-height: calc(100vh - 180px);
  
  &:hover {
    box-shadow: 0 8px 32px rgba(106, 85, 255, 0.08);
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px 28px;
  background: white;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  position: relative;
  border-left: 4px solid #667eea;
  flex-shrink: 0;
}

.header-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #667eea;
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

.card-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  outline: none;
  
  &.add-btn {
    background: linear-gradient(135deg, #667eea, #764ba2);
    color: white;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
    }
    
    svg {
      stroke: white;
    }
  }
}

/* 工具栏 */
.filter-toolbar {
  padding: 20px 28px 0;
  background: white;
  flex-shrink: 0;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-label {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a2e;
  white-space: nowrap;
  padding: 4px 8px;
  background: rgba(106, 85, 255, 0.1);
  border-radius: 8px;
  color: #6a55ff;
}

.filter-actions {
  display: flex;
  gap: 12px;
  flex: 1;
  flex-wrap: wrap;
  align-items: center;
}

.filter-select {
  min-width: 140px;
  
  :deep(.el-input__wrapper) {
    border-radius: 10px;
    border: 1px solid rgba(0, 0, 0, 0.1);
    background: rgba(248, 247, 255, 0.5);
    transition: all 0.3s ease;
    
    &:hover {
      border-color: #6a55ff;
      box-shadow: 0 0 0 3px rgba(106, 85, 255, 0.1);
    }
  }
}

.search-input {
  min-width: 140px;
  max-width: 200px;
  
  :deep(.el-input__wrapper) {
    border-radius: 10px;
    border: 1px solid rgba(0, 0, 0, 0.1);
    background: rgba(248, 247, 255, 0.5);
    transition: all 0.3s ease;
    
    &:hover {
      border-color: #6a55ff;
      box-shadow: 0 0 0 3px rgba(106, 85, 255, 0.1);
    }
  }
}

.search-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(106, 85, 255, 0.3);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(106, 85, 255, 0.4);
  }
  
  svg {
    stroke: white;
  }
}

/* 表格容器 */
.table-container {
  padding: 0 28px 20px;
  flex: 1;
  overflow: auto;
  min-width: 0; /* 确保表格容器不会溢出 */
  min-height: 400px;
}

:deep(.table-header) {
  th {
    background: rgba(248, 247, 255, 0.5) !important;
    border: none;
    padding: 12px 16px;
    font-weight: 600;
    color: #1a1a2e;
    font-size: 13px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    white-space: nowrap;
    
    &.is-leaf {
      border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    }
  }
}

:deep(.table-row) {
  td {
    padding: 16px;
    border: none;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    transition: all 0.2s ease;
    white-space: nowrap;
  }
  
  &:hover {
    td {
      background: rgba(106, 85, 255, 0.03);
    }
  }
}

/* 表格单元格样式 */
.id-cell {
  font-size: 13px;
  font-weight: 600;
  color: #666;
  background: rgba(106, 85, 255, 0.1);
  padding: 4px 8px;
  border-radius: 8px;
  text-align: center;
  width: fit-content;
}

.account-cell {
  .account {
    font-weight: 500;
    color: #1a1a2e;
  }
}

.name-cell {
  .name {
    font-weight: 500;
    color: #1a1a2e;
  }
}

.class-cell {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.role-badge {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  width: fit-content;
  
  &.student {
    background: rgba(106, 85, 255, 0.1);
    color: #6a55ff;
    border: 1px solid rgba(106, 85, 255, 0.2);
  }
  
  &.counselor {
    background: rgba(255, 107, 107, 0.1);
    color: #ff6b6b;
    border: 1px solid rgba(255, 107, 107, 0.2);
  }
  
  &.admin {
    background: rgba(78, 205, 196, 0.1);
    color: #4ecdc4;
    border: 1px solid rgba(78, 205, 196, 0.2);
  }
}

.status-badge {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  width: fit-content;
  
  &.active {
    background: rgba(78, 205, 196, 0.1);
    color: #4ecdc4;
    border: 1px solid rgba(78, 205, 196, 0.2);
  }
  
  &.muted {
    background: rgba(255, 230, 109, 0.1);
    color: #d4a017;
    border: 1px solid rgba(255, 230, 109, 0.2);
  }
}

/* 操作按钮 - 调整避免遮挡 */
.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  flex-wrap: nowrap; /* 防止换行 */
  min-width: 0; /* 允许缩小 */
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px; /* 减小内边距 */
  border-radius: 8px;
  font-size: 12px; /* 减小字体 */
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: white;
  white-space: nowrap;
  flex-shrink: 0; /* 防止按钮被压缩 */
  
  svg {
    stroke: currentColor;
    width: 12px; /* 减小图标 */
    height: 12px;
  }
  
  &:hover {
    transform: translateY(-1px);
  }
  
  &.status-btn {
    &.mute {
      color: #666;
      border-color: rgba(0, 0, 0, 0.1);
      
      &:hover {
        background: rgba(255, 230, 109, 0.1);
        color: #d4a017;
        border-color: rgba(255, 230, 109, 0.2);
      }
    }
    
    &.unmute {
      color: #666;
      border-color: rgba(0, 0, 0, 0.1);
      
      &:hover {
        background: rgba(78, 205, 196, 0.1);
        color: #4ecdc4;
        border-color: rgba(78, 205, 196, 0.2);
      }
    }
  }
  
  &.delete-btn {
    color: #666;
    border-color: rgba(0, 0, 0, 0.1);
    
    &:hover {
      background: rgba(255, 107, 107, 0.1);
      color: #ff6b6b;
      border-color: rgba(255, 107, 107, 0.2);
    }
  }
}

/* 分页 */
.pagination-container {
  padding: 20px 28px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: center;
  flex-shrink: 0;
  
  :deep(.el-pagination) {
    .el-pager li {
      border-radius: 8px;
      margin: 0 2px;
    }
    
    .btn-prev,
    .btn-next {
      border-radius: 8px;
    }
  }
}

/* 表单对话框 */
.form-dialog {
  :deep(.el-dialog) {
    border-radius: 20px;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  }
  
  :deep(.el-dialog__header) {
    padding: 24px 28px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    margin-right: 0;
    
    .el-dialog__title {
      font-size: 20px;
      font-weight: 600;
      color: #1a1a2e;
    }
  }
  
  :deep(.el-dialog__body) {
    padding: 0;
  }
  
  :deep(.el-dialog__headerbtn) {
    top: 24px;
    right: 28px;
  }
}

.form-content {
  padding: 28px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  
  &.full-width {
    grid-column: 1 / -1;
  }
}

.form-label {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-input {
  :deep(.el-input__wrapper) {
    border-radius: 10px;
    border: 1px solid rgba(0, 0, 0, 0.1);
    background: rgba(248, 247, 255, 0.5);
    padding: 8px 12px;
    transition: all 0.3s ease;
    
    &:hover {
      border-color: #6a55ff;
      box-shadow: 0 0 0 3px rgba(106, 85, 255, 0.1);
    }
  }
  
  :deep(.el-textarea__inner) {
    border-radius: 10px;
    border: 1px solid rgba(0, 0, 0, 0.1);
    background: rgba(248, 247, 255, 0.5);
    padding: 12px;
    transition: all 0.3s ease;
    font-family: inherit;
    
    &:hover {
      border-color: #6a55ff;
      box-shadow: 0 0 0 3px rgba(106, 85, 255, 0.1);
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 28px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.dialog-btn {
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  min-width: 100px;
}

.cancel-btn {
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.1);
  color: #666;
  
  &:hover {
    background: rgba(0, 0, 0, 0.05);
  }
}

.submit-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .management-card {
    border-radius: 16px;
  }
  
  .card-header {
    padding: 20px;
    flex-wrap: wrap;
  }
  
  .header-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .header-content h3 {
    font-size: 18px;
  }
  
  .card-actions {
    width: 100%;
    margin-top: 12px;
    justify-content: flex-end;
  }
  
  .filter-toolbar {
    padding: 16px 20px 0;
  }
  
  .filter-group {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-label {
    align-self: flex-start;
  }
  
  .filter-actions {
    flex-direction: column;
  }
  
  .filter-select,
  .search-input {
    width: 100%;
    max-width: none;
  }
  
  .table-container {
    padding: 0 20px 20px;
  }
  
  .action-buttons {
    flex-wrap: wrap; /* 在小屏幕上允许换行 */
  }
  
  .form-content {
    padding: 20px;
  }
  
  .pagination-container {
    padding: 16px 20px;
  }
}

/* 针对中等屏幕的调整 */
@media (max-width: 1200px) and (min-width: 769px) {
  .action-buttons {
    gap: 4px; /* 减小间距 */
  }
  
  .action-btn {
    padding: 5px 8px; /* 进一步减小内边距 */
    font-size: 11px; /* 进一步减小字体 */
    
    svg {
      width: 10px;
      height: 10px;
    }
  }
}
</style>