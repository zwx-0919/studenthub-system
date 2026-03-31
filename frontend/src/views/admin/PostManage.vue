<template>
  <div class="post-management-page">
    <!-- 帖子管理卡片 -->
    <div class="management-card">
      <div class="card-header">
        <div class="header-icon">📝</div>
        <div class="header-content">
          <h3>帖子管理</h3>
          <p>管理系统用户发布的帖子内容</p>
        </div>
        <div class="card-actions">
          <button class="action-btn add-btn" @click="openAdd">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
            新增帖子
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
              v-model="query.keyword"
              placeholder="搜索标题/内容"
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

      <!-- 帖子表格 -->
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
          
          <el-table-column prop="title" label="帖子标题" min-width="180">
            <template #default="{ row }">
              <div class="title-cell">
                <div class="title">{{ row.title }}</div>
                <div v-if="row.category" class="category">🏷️ {{ row.category }}</div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="图片" width="220">
            <template #default="{ row }">
              <div v-if="parseImageList(row.imageUrl).length" class="table-image-grid">
                <el-image
                  v-for="(img, idx) in parseImageList(row.imageUrl).slice(0, 4)"
                  :key="img + idx"
                  class="table-image"
                  :src="img"
                  :preview-src-list="parseImageList(row.imageUrl)"
                  :initial-index="idx"
                  fit="cover"
                  preview-teleported
                />
                <div v-if="parseImageList(row.imageUrl).length > 4" class="image-count">
                  +{{ parseImageList(row.imageUrl).length - 4 }}
                </div>
              </div>
              <div v-else class="no-image">无图片</div>
            </template>
          </el-table-column>
          
          <el-table-column prop="userName" label="作者" width="120">
            <template #default="{ row }">
              <div class="author-cell">
                <div class="avatar">👤</div>
                <div class="author-name">{{ row.userName }}</div>
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
          
          <el-table-column label="互动" width="140">
            <template #default="{ row }">
              <div class="interaction-cell">
                <div class="like-count">❤️ {{ row.likeCount || 0 }}</div>
                <div class="comment-count">💬 {{ row.commentCount || 0 }}</div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="320" align="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <button
                  v-if="row.status === 1"
                  class="action-btn unpublish-btn"
                  @click="toggleStatus(row, 0)"
                  title="下架帖子"
                >
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"></line>
                  </svg>
                  下架
                </button>
                <button
                  v-else
                  class="action-btn publish-btn"
                  @click="toggleStatus(row, 1)"
                  title="发布帖子"
                >
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                    <polyline points="22 4 12 14.01 9 11.01"></polyline>
                  </svg>
                  发布
                </button>
                <button class="action-btn edit-btn" @click="openEdit(row)" title="编辑">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                  </svg>
                  编辑
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

    <!-- 帖子弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editId ? '编辑帖子' : '新增帖子'"
      width="600px"
      class="form-dialog"
    >
      <div class="form-content">
        <div class="form-grid">
          <div class="form-group">
            <label class="form-label">分类</label>
            <el-input
              v-model="form.category"
              placeholder="请输入帖子分类"
              class="form-input"
            />
          </div>
          
          <div class="form-group full-width">
            <label class="form-label">标题</label>
            <el-input
              v-model="form.title"
              placeholder="请输入帖子标题"
              class="form-input"
            />
          </div>
          
          <div class="form-group full-width">
            <label class="form-label">内容</label>
            <el-input
              v-model="form.content"
              type="textarea"
              rows="6"
              placeholder="请输入帖子内容"
              class="form-input"
              resize="none"
            />
          </div>
          
          <div class="form-group full-width">
            <label class="form-label">图片地址</label>
            <el-input
              v-model="form.imageUrl"
              type="textarea"
              rows="3"
              placeholder="可粘贴多个图片URL，使用英文逗号分隔"
              class="form-input"
            />
            <div class="form-tip">支持多图：url1,url2,url3</div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <button class="dialog-btn cancel-btn" @click="dialogVisible = false">取消</button>
          <button class="dialog-btn submit-btn" @click="submit">保存</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import request from "@/utils/request";

const normalizeImageUrl = (imageUrl) => {
  if (!imageUrl) return "";
  return String(imageUrl)
    .split(",")
    .map((x) => x.trim())
    .filter(Boolean)
    .join(",");
};

const rows = ref([]);
const total = ref(0);
const dialogVisible = ref(false);
const editId = ref(null);
const form = reactive({ category: "", title: "", content: "", imageUrl: "" });
const query = reactive({
  current: 1,
  size: 10,
  status: undefined,
  keyword: ""
});

const statusText = (v) => (v === 1 ? "已发布" : v === 0 ? "已下架" : String(v ?? ""));

const parseImageList = (imageUrl) => {
  if (!imageUrl) return [];
  return String(imageUrl)
    .split(",")
    .map((x) => x.trim())
    .filter(Boolean);
};

const load = async () => {
  const res = await request.get("/api/admin/post-manage/page", {
    params: { current: query.current, size: query.size, status: query.status, keyword: query.keyword }
  });
  rows.value = res.data?.records || [];
  total.value = res.data?.total || 0;
};

const handleSizeChange = (size) => {
  query.size = size;
  query.current = 1;
  load();
};

const toggleStatus = async (row, status) => {
  await request.patch(`/api/admin/post-manage/${row.id}/status`, null, {
    params: { status }
  });
  ElMessage.success("操作成功");
  load();
};

const openAdd = () => {
  editId.value = null;
  form.category = "";
  form.title = "";
  form.content = "";
  form.imageUrl = "";
  dialogVisible.value = true;
};

const openEdit = (row) => {
  editId.value = row.id;
  form.category = row.category || "";
  form.title = row.title || "";
  form.content = row.content || "";
  form.imageUrl = row.imageUrl || "";
  dialogVisible.value = true;
};

const submit = async () => {
  const payload = {
    ...form,
    imageUrl: normalizeImageUrl(form.imageUrl)
  };
  if (editId.value) {
    await request.put(`/api/admin/post-manage/${editId.value}`, payload);
  } else {
    await request.post("/api/admin/post-manage", payload);
  }
  ElMessage.success("操作成功");
  dialogVisible.value = false;
  load();
};

const del = async (row) => {
  await ElMessageBox.confirm("确定删除该帖子吗？", "提示", {
    type: "warning"
  });
  await request.delete("/api/admin/post-manage", { data: [row.id] });
  ElMessage.success("删除成功");
  load();
};

onMounted(load);
</script>

<style scoped lang="less">
.post-management-page {
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
  
  .category {
    font-size: 12px;
    color: #666;
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

/* 图片网格样式 */
.table-image-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  position: relative;
}

.table-image {
  width: 100%;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.2s ease;
  
  &:hover {
    transform: scale(1.05);
  }
}

.image-count {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
}

.no-image {
  font-size: 12px;
  color: #999;
  text-align: center;
  padding: 8px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 8px;
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

.interaction-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  
  .like-count,
  .comment-count {
    font-size: 12px;
    color: #666;
    display: flex;
    align-items: center;
    gap: 4px;
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
  
  &.edit-btn {
    color: #666;
    border-color: rgba(0, 0, 0, 0.1);
    
    &:hover {
      background: rgba(106, 85, 255, 0.08);
      color: #6a55ff;
      border-color: rgba(106, 85, 255, 0.2);
    }
  }
  
  &.publish-btn {
    color: #666;
    border-color: rgba(0, 0, 0, 0.1);
    
    &:hover {
      background: rgba(78, 205, 196, 0.1);
      color: #4ecdc4;
      border-color: rgba(78, 205, 196, 0.2);
    }
  }
  
  &.unpublish-btn {
    color: #666;
    border-color: rgba(0, 0, 0, 0.1);
    
    &:hover {
      background: rgba(255, 230, 109, 0.1);
      color: #d4a017;
      border-color: rgba(255, 230, 109, 0.2);
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
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  line-height: 1.4;
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