<template>
  <div class="notice-management-page">
    <!-- 公告管理卡片 -->
    <div class="management-card">
      <div class="card-header">
        <div class="header-icon">📢</div>
        <div class="header-content">
          <h3>公告管理</h3>
          <p>管理系统公告发布和状态</p>
        </div>
        <div class="card-actions">
          <button class="action-btn add-btn" @click="openAdd">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
            新增公告
          </button>
        </div>
      </div>

      <!-- 过滤工具栏 -->
      <div class="filter-toolbar">
        <div class="filter-group">
          <div class="filter-label">筛选条件</div>
          <div class="filter-actions">
            <el-select
              v-model="query.status"
              placeholder="状态筛选"
              clearable
              class="filter-select"
            >
              <el-option label="已发布" :value="1" />
              <el-option label="已下架" :value="0" />
            </el-select>
            <el-input
              v-model="query.titleKeyword"
              placeholder="标题关键词"
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

      <!-- 公告表格 -->
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
          
          <el-table-column prop="title" label="公告标题" min-width="200">
            <template #default="{ row }">
              <div class="title-cell">
                <div class="title">{{ row.title }}</div>
                <div v-if="row.content" class="preview">{{ row.content.substring(0, 30) }}...</div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="authorName" label="作者" width="120">
            <template #default="{ row }">
              <div class="author-cell">
                <div class="avatar">👤</div>
                <div class="author-name">{{ row.authorName }}</div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <div class="status-badge" :class="{ 'published': row.status === 1, 'unpublished': row.status !== 1 }">
                {{ statusText(row.status) }}
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="统计" width="120">
            <template #default="{ row }">
              <div class="stats-cell">
                <div class="view-count">👁️ {{ row.viewCount || 0 }}</div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="publishTime" label="发布时间" width="170">
            <template #default="{ row }">
              <div class="time-cell">{{ formatTime(row.publishTime) }}</div>
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
                  :class="{ 'unpublish': row.status === 1, 'publish': row.status !== 1 }"
                  @click="toggleStatus(row)"
                  :title="row.status === 1 ? '下架公告' : '发布公告'"
                >
                  <svg v-if="row.status === 1" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"></line>
                  </svg>
                  <svg v-else viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                    <polyline points="22 4 12 14.01 9 11.01"></polyline>
                  </svg>
                  {{ row.status === 1 ? "下架" : "发布" }}
                </button>
                <button class="action-btn delete-btn" @click="del(row)" title="删除">
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

    <!-- 公告弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editId ? '编辑公告' : '新增公告'"
      width="800px"
      class="form-dialog"
      @close="resetForm"
    >
      <div class="form-content">
        <div class="form-grid">
          <div class="form-group full-width">
            <label class="form-label">公告标题</label>
            <el-input
              v-model="form.title"
              placeholder="请输入公告标题"
              class="form-input"
            />
          </div>
          
          <div class="form-group full-width">
            <label class="form-label">公告内容</label>
            <el-input
              v-model="form.content"
              type="textarea"
              rows="8"
              placeholder="请输入公告正文"
              class="form-input"
              resize="none"
            />
          </div>
          
          <div class="form-group full-width">
            <label class="form-label">图片上传</label>
            <div class="upload-container">
              <el-upload
                list-type="picture-card"
                :http-request="uploadNoticeImage"
                :file-list="imageFileList"
                :on-remove="handleImageRemove"
                :limit="9"
                multiple
                accept="image/*"
                class="image-upload"
              >
                <div class="upload-trigger">
                  <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="17 8 12 3 7 8"></polyline>
                    <line x1="12" y1="3" x2="12" y2="15"></line>
                  </svg>
                  <div class="upload-text">上传图片</div>
                </div>
              </el-upload>
              <div class="upload-hint">支持 JPG/PNG 格式，最多上传 9 张图片</div>
            </div>
          </div>
          
          <div class="form-group">
            <label class="form-label">发布时间</label>
            <el-date-picker
              v-model="form.publishTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择发布时间"
              class="form-input"
              :teleported="false"
            />
          </div>
          
          <div v-if="editId" class="form-group">
            <label class="form-label">状态</label>
            <el-select
              v-model="form.status"
              placeholder="请选择状态"
              class="form-input"
            >
              <el-option label="已发布" :value="1" />
              <el-option label="已下架" :value="0" />
            </el-select>
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
import { useUserStore } from "@/store/user";

const store = useUserStore();
const rows = ref([]);
const total = ref(0);
const dialogVisible = ref(false);
const editId = ref(null);
const imageFileList = ref([]);
const query = reactive({
  current: 1,
  size: 10,
  status: undefined,
  titleKeyword: ""
});
const form = reactive({
  title: "",
  content: "",
  imageUrl: "",
  publishTime: "",
  status: 1
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

const statusText = (v) => (v === 1 ? "已发布" : v === 0 ? "已下架" : String(v ?? ""));

const parseImageUrls = (raw) =>
  String(raw || "")
    .split(",")
    .map((x) => x.trim())
    .filter(Boolean);

const syncImageUrlField = () => {
  form.imageUrl = imageFileList.value.map((f) => f.url).filter(Boolean).join(",");
};

const uploadNoticeImage = async (options) => {
  try {
    const data = new FormData();
    data.append("file", options.file);
    const res = await request.post("/user/avatar/upload", data, {
      headers: { "Content-Type": "multipart/form-data" }
    });
    const url = res.data?.url;
    if (!url) throw new Error("上传失败");
    imageFileList.value.push({
      name: options.file.name,
      url
    });
    syncImageUrlField();
    options.onSuccess?.({ url });
  } catch (e) {
    options.onError?.(e);
    ElMessage.error("图片上传失败");
  }
};

const handleImageRemove = (file) => {
  imageFileList.value = imageFileList.value.filter((x) => x.url !== file.url);
  syncImageUrlField();
};

const load = async () => {
  const res = await request.get("/notice/notice", {
    params: {
      current: query.current,
      size: query.size,
      status: query.status,
      titleKeyword: query.titleKeyword
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

const openAdd = () => {
  editId.value = null;
  form.title = "";
  form.content = "";
  form.imageUrl = "";
  imageFileList.value = [];
  form.publishTime = "";
  form.status = 1;
  dialogVisible.value = true;
};

const openEdit = (row) => {
  editId.value = row.id;
  form.title = row.title;
  form.content = row.content;
  form.imageUrl = row.imageUrl || "";
  imageFileList.value = parseImageUrls(row.imageUrl).map((url, idx) => ({
    name: `image_${idx + 1}`,
    url
  }));
  form.publishTime = row.publishTime || "";
  form.status = row.status;
  dialogVisible.value = true;
};

const resetForm = () => {
  editId.value = null;
  imageFileList.value = [];
};

const submit = async () => {
  if (!form.title?.trim()) {
    ElMessage.warning("请输入标题");
    return;
  }
  if (editId.value) {
    await request.put(`/notice/notice/${editId.value}`, {
      ...form,
      authorId: store.user?.id,
      authorName: store.user?.userName
    });
    ElMessage.success("更新成功");
  } else {
    await request.post("/notice/add", {
      ...form,
      authorId: store.user?.id,
      authorName: store.user?.userName
    });
    ElMessage.success("新增成功");
  }
  dialogVisible.value = false;
  load();
};

const toggleStatus = async (row) => {
  await request.patch(`/notice/notice/${row.id}`);
  ElMessage.success("操作成功");
  load();
};

const del = async (row) => {
  await ElMessageBox.confirm("确定删除该公告吗？", "提示", { type: "warning" });
  await request.delete("/notice", { data: [row.id] });
  ElMessage.success("删除成功");
  load();
};

onMounted(load);
</script>

<style scoped lang="less">
.notice-management-page {
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
  border-left: 4px solid #4ecdc4;
}

.header-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(78, 205, 196, 0.1), rgba(68, 160, 141, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #4ecdc4;
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
    background: linear-gradient(135deg, #4ecdc4, #44a08d);
    color: white;
    box-shadow: 0 4px 12px rgba(78, 205, 196, 0.3);
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(78, 205, 196, 0.4);
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
  flex: 1;
  min-width: 200px;
  max-width: 300px;
  
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

.title-cell {
  .title {
    font-weight: 500;
    color: #1a1a2e;
    margin-bottom: 4px;
    line-height: 1.4;
  }
  
  .preview {
    font-size: 12px;
    color: #666;
    line-height: 1.4;
  }
}

.author-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .avatar {
    width: 24px;
    height: 24px;
    border-radius: 6px;
    background: rgba(106, 85, 255, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
  }
  
  .author-name {
    font-weight: 500;
    color: #666;
  }
}

.status-badge {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  width: fit-content;
  
  &.published {
    background: rgba(78, 205, 196, 0.1);
    color: #4ecdc4;
    border: 1px solid rgba(78, 205, 196, 0.2);
  }
  
  &.unpublished {
    background: rgba(255, 230, 109, 0.1);
    color: #d4a017;
    border: 1px solid rgba(255, 230, 109, 0.2);
  }
}

.stats-cell {
  .view-count {
    font-size: 12px;
    color: #666;
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.time-cell {
  font-size: 12px;
  color: #666;
  font-weight: 500;
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
    &.unpublish {
      color: #666;
      border-color: rgba(0, 0, 0, 0.1);
      
      &:hover {
        background: rgba(255, 230, 109, 0.1);
        color: #d4a017;
        border-color: rgba(255, 230, 109, 0.2);
      }
    }
    
    &.publish {
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
  
  :deep(.el-range__icon),
  :deep(.el-range__close-icon) {
    display: none;
  }
}

/* 上传组件 */
.upload-container {
  .image-upload {
    :deep(.el-upload) {
      width: 100px;
      height: 100px;
      border-radius: 10px;
      border: 1px dashed rgba(0, 0, 0, 0.1);
      background: rgba(248, 247, 255, 0.5);
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &:hover {
        border-color: #6a55ff;
        background: rgba(106, 85, 255, 0.05);
      }
    }
    
    :deep(.el-upload-list__item) {
      width: 100px;
      height: 100px;
      border-radius: 10px;
      border: 1px solid rgba(0, 0, 0, 0.1);
    }
  }
  
  .upload-trigger {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #666;
  }
  
  .upload-text {
    font-size: 12px;
  }
  
  .upload-hint {
    font-size: 12px;
    color: #999;
    margin-top: 8px;
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
  background: linear-gradient(135deg, #4ecdc4, #44a08d);
  color: white;
  box-shadow: 0 4px 12px rgba(78, 205, 196, 0.3);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(78, 205, 196, 0.4);
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