<template>
  <div class="course-management-page">
    <!-- 课程管理卡片 -->
    <div class="management-card">
      <div class="card-header">
        <div class="header-icon">📚</div>
        <div class="header-content">
          <h3>课程管理</h3>
          <p>管理系统课程信息和安排</p>
        </div>
        <div class="card-actions">
          <button class="action-btn refresh-btn" @click="loadCourses">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="23 4 23 10 17 10"></polyline>
              <polyline points="1 20 1 14 7 14"></polyline>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
            </svg>
            刷新
          </button>
          <button class="action-btn add-btn" @click="openAdd">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
            新增课程
          </button>
        </div>
      </div>

      <!-- 过滤工具栏 -->
      <div class="filter-toolbar">
        <div class="filter-group">
          <div class="filter-label">筛选条件</div>
          <div class="filter-actions">
            <el-select
              v-model="query.majorId"
              placeholder="专业筛选"
              clearable
              class="filter-select"
            >
              <el-option v-for="m in majorOptions" :key="m.id" :label="m.name" :value="m.id" />
            </el-select>
            <el-select
              v-model="query.status"
              placeholder="状态筛选"
              clearable
              class="filter-select"
            >
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </div>
        </div>
      </div>

      <!-- 课程表格 -->
      <div class="table-container">
        <el-table
          :data="courses"
          style="width: 100%"
          row-class-name="table-row"
          header-row-class-name="table-header"
        >
          <el-table-column prop="id" label="ID" width="80">
            <template #default="{ row }">
              <div class="id-cell">#{{ row.id }}</div>
            </template>
          </el-table-column>
          
          <el-table-column prop="courseName" label="课程名称" min-width="180">
            <template #default="{ row }">
              <div class="course-name-cell">
                <div class="name">{{ row.courseName }}</div>
                <div v-if="row.teacherName" class="teacher">👨‍🏫 {{ row.teacherName }}</div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="上课信息" min-width="200">
            <template #default="{ row }">
              <div class="info-cell">
                <div v-if="row.location" class="location">
                  <svg class="location-icon" viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                    <circle cx="12" cy="10" r="3"></circle>
                  </svg>
                  {{ row.location }}
                </div>
                <div v-if="row.time" class="time">
                  <svg class="time-icon" viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <polyline points="12 6 12 12 16 14"></polyline>
                  </svg>
                  {{ formatTime(row.time) }}
                </div>
                <div v-if="row.classNo" class="class">🏫 {{ row.classNo }}</div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <div class="status-badge" :class="{ 'enabled': row.status === 1, 'disabled': row.status !== 1 }">
                {{ statusText(row.status) }}
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="320" align="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <button class="action-btn edit-btn" @click="openEdit(row)" title="编辑">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                  </svg>
                  编辑
                </button>
                <button
                  class="action-btn status-btn"
                  :class="{ 'disable': row.status === 1, 'enable': row.status !== 1 }"
                  @click="toggleStatus(row)"
                  :title="row.status === 1 ? '禁用课程' : '启用课程'"
                >
                  <svg v-if="row.status === 1" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"></line>
                  </svg>
                  <svg v-else viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                    <polyline points="22 4 12 14.01 9 11.01"></polyline>
                  </svg>
                  {{ row.status === 1 ? "禁用" : "启用" }}
                </button>
                <button class="action-btn delete-btn" @click="delCourse(row)" title="删除">
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
    </div>

    <!-- 弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editId ? '编辑课程' : '新增课程'"
      width="600px"
      class="form-dialog"
    >
      <div class="form-content">
        <div class="form-grid">
          <div class="form-group">
            <label class="form-label">课程名称</label>
            <el-input
              v-model="form.courseName"
              placeholder="请输入课程名称"
              class="form-input"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">上课地点</label>
            <el-input
              v-model="form.location"
              placeholder="请输入上课地点"
              class="form-input"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">上课时间</label>
            <el-date-picker
              v-model="form.time"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择上课时间"
              class="form-input"
              :teleported="false"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">班级</label>
            <el-input
              v-model="form.classNo"
              placeholder="请输入班级"
              class="form-input"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">专业</label>
            <el-select
              v-model="form.majorId"
              placeholder="请选择专业"
              class="form-input"
            >
              <el-option v-for="m in majorOptions" :key="m.id" :label="m.name" :value="m.id" />
            </el-select>
          </div>
          
          <div class="form-group">
            <label class="form-label">教师姓名</label>
            <el-input
              v-model="form.teacherName"
              placeholder="请输入教师姓名"
              class="form-input"
            />
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <button class="dialog-btn cancel-btn" @click="dialogVisible = false">取消</button>
          <button class="dialog-btn submit-btn" @click="submit">确定</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import request from "@/utils/request";

const courses = ref([]);
const majorOptions = ref([]);
const dialogVisible = ref(false);
const editId = ref(null);
const query = reactive({ majorId: undefined, status: undefined });
const form = reactive({
  courseName: "",
  location: "",
  time: "",
  classNo: "",
  teacherName: "",
  majorId: undefined
});

// 格式化时间显示
const formatTime = (v) => {
  if (!v) return "";
  const date = new Date(v);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).replace(/\//g, '-');
};

const statusText = (v) => (v === 1 ? "启用" : v === 0 ? "禁用" : String(v ?? ""));

const loadCourses = async () => {
  const res = await request.get("/api/admin/course/page", { params: { current: 1, size: 50, ...query } });
  courses.value = res.data?.records || [];
};

const openAdd = () => {
  editId.value = null;
  Object.assign(form, { courseName: "", location: "", time: "", classNo: "", teacherName: "", majorId: undefined });
  dialogVisible.value = true;
};

const openEdit = (row) => {
  editId.value = row.id;
  Object.assign(form, {
    courseName: row.courseName || "",
    location: row.location || "",
    time: row.time || "",
    classNo: row.classNo || "",
    teacherName: row.teacherName || "",
    majorId: row.majorId
  });
  dialogVisible.value = true;
};

const submit = async () => {
  if (!form.courseName?.trim()) return ElMessage.warning("请输入课程名");
  if (!form.majorId) return ElMessage.warning("请选择专业");
  if (editId.value) {
    await request.put(`/api/admin/course/${editId.value}`, form);
  } else {
    await request.post("/api/admin/course/add", form);
  }
  ElMessage.success("操作成功");
  dialogVisible.value = false;
  loadCourses();
};

const toggleStatus = async (row) => {
  await request.patch(`/api/admin/course/${row.id}`);
  ElMessage.success("操作成功");
  loadCourses();
};

const delCourse = async (row) => {
  await ElMessageBox.confirm("确定删除该课程吗？", "提示", { type: "warning" });
  await request.delete("/api/admin/course", { data: [row.id] });
  ElMessage.success("删除成功");
  loadCourses();
};

onMounted(async () => {
  await loadCourses();
  const res = await request.get("/api/admin/base/majors");
  majorOptions.value = res.data || [];
});
</script>

<style scoped lang="less">
.course-management-page {
  max-width: 100%;
  padding: 0;
  background: #f8f9ff;
  min-height: 100vh;
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
  border-left: 4px solid #6a55ff;
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
  
  &.refresh-btn {
    background: rgba(248, 247, 255, 0.8);
    border: 1px solid rgba(106, 85, 255, 0.1);
    color: #666;
    
    &:hover {
      background: rgba(106, 85, 255, 0.08);
      color: #6a55ff;
      transform: translateY(-1px);
    }
    
    svg {
      stroke: currentColor;
    }
  }
  
  &.add-btn {
    background: linear-gradient(135deg, #6a55ff, #8a7aff);
    color: white;
    box-shadow: 0 4px 12px rgba(106, 85, 255, 0.3);
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(106, 85, 255, 0.4);
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

/* 表格容器 */
.table-container {
  padding: 0 28px 28px;
  flex: 1;
  overflow: hidden;
  min-width: 0; /* 确保表格容器不会溢出 */
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

.course-name-cell {
  .name {
    font-weight: 500;
    color: #1a1a2e;
    margin-bottom: 4px;
    line-height: 1.4;
  }
  
  .teacher {
    font-size: 12px;
    color: #666;
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.info-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
  
  .location,
  .time,
  .class {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #666;
    
    .location-icon,
    .time-icon {
      stroke: #999;
      flex-shrink: 0;
    }
  }
}

.status-badge {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  width: fit-content;
  
  &.enabled {
    background: rgba(78, 205, 196, 0.1);
    color: #4ecdc4;
    border: 1px solid rgba(78, 205, 196, 0.2);
  }
  
  &.disabled {
    background: rgba(255, 107, 107, 0.1);
    color: #ff6b6b;
    border: 1px solid rgba(255, 107, 107, 0.2);
  }
}

/* 操作按钮 - 主要修改这里 */
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
  
  &.edit-btn {
    color: #666;
    border-color: rgba(0, 0, 0, 0.1);
    
    &:hover {
      background: rgba(106, 85, 255, 0.08);
      color: #6a55ff;
      border-color: rgba(106, 85, 255, 0.2);
    }
  }
  
  &.status-btn {
    &.disable {
      color: #666;
      border-color: rgba(0, 0, 0, 0.1);
      
      &:hover {
        background: rgba(255, 230, 109, 0.1);
        color: #d4a017;
        border-color: rgba(255, 230, 109, 0.2);
      }
    }
    
    &.enable {
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
  
  :deep(.el-range__icon),
  :deep(.el-range__close-icon) {
    display: none;
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
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  box-shadow: 0 4px 12px rgba(106, 85, 255, 0.3);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(106, 85, 255, 0.4);
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
  
  .filter-select {
    width: 100%;
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