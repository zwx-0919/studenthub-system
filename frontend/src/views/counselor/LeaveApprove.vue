<template>
  <div class="leave-approval-container">
    <el-card class="approval-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="card-title">待审批请假</span>
            <el-tag type="warning" size="small" class="pending-count">
              待处理: {{ pendingCount }}
            </el-tag>
          </div>
          <div class="header-right">
            <el-button type="info" size="small" plain @click="resetQuery">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
            <el-button type="primary" size="small" @click="load" :loading="loading">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
          </div>
        </div>
      </template>

      <div class="filter-container">
        <div class="filter-row">
          <div class="filter-group">
            <div class="filter-item">
              <div class="filter-label">班级筛选</div>
              <el-select
                v-model="query.classId"
                placeholder="请选择班级"
                clearable
                filterable
                class="filter-select"
              >
                <el-option
                  v-for="c in classOptions"
                  :key="c.id"
                  :label="c.name"
                  :value="c.id"
                />
              </el-select>
            </div>
            
            <div class="filter-item">
              <div class="filter-label">时间范围</div>
              <el-date-picker
                v-model="query.dates"
                type="datetimerange"
                value-format="YYYY-MM-DD HH:mm:ss"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                class="filter-date"
              />
            </div>
          </div>
        </div>
      </div>

      <div class="table-container">
        <el-table
          :data="rows"
          style="width: 100%"
          stripe
          v-loading="loading"
          header-row-class-name="table-header"
          :border="true"
        >
          <el-table-column prop="id" label="申请ID" width="100" fixed="left">
            <template #default="{ row }">
              <div class="apply-id">#{{ String(row.id).padStart(4, '0') }}</div>
            </template>
          </el-table-column>
          
          <el-table-column prop="studentName" label="学生信息" width="150">
            <template #default="{ row }">
              <div class="student-info">
                <div class="student-name">{{ row.studentName }}</div>
                <el-tag size="small" type="info" class="class-tag">{{ row.className }}</el-tag>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="reason" label="请假理由" min-width="300">
            <template #default="{ row }">
              <div class="reason-content">
                {{ row.reason || '无' }}
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="getStatusType(row.approveStatus)"
                size="small"
                class="status-tag"
                effect="light"
              >
                {{ statusText(row.approveStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="请假凭证" width="120" align="center">
            <template #default="{ row }">
              <div class="proof-wrap">
                <div
                  v-if="row.proofUrl"
                  class="image-preview"
                  @click="preview = row.proofUrl"
                >
                  <img
                    :src="row.proofUrl"
                    class="thumb-img"
                    :alt="`${row.studentName}的请假凭证`"
                  />
                  <div class="image-overlay">
                    <el-icon><ZoomIn /></el-icon>
                  </div>
                </div>
                <div v-else class="no-image">
                  <el-icon><Picture /></el-icon>
                  <span>无凭证</span>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  type="success"
                  size="small"
                  @click="approve(row.id, 1)"
                  :icon="Check"
                  class="approve-btn"
                >
                  通过
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="approve(row.id, 2)"
                  :icon="Close"
                  class="reject-btn"
                >
                  驳回
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="rows.length === 0 && !loading" class="empty-state">
          <el-icon class="empty-icon"><Document /></el-icon>
          <div class="empty-text">暂无待审批的请假申请</div>
          <div class="empty-subtext">符合条件的请假申请将在这里显示</div>
        </div>
      </div>
    </el-card>

    <!-- 图片预览对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="`请假凭证预览`"
      width="800px"
      align-center
      class="preview-dialog"
      @close="closePreview"
    >
      <div class="preview-container">
        <div class="image-viewer" @wheel.prevent="handleWheel">
          <img
            v-if="preview"
            :src="preview"
            :key="preview"
            :style="{ transform: `scale(${imgScale})` }"
            class="preview-image"
            @load="handleImageLoad"
          />
          <div v-else class="image-loading">
            <el-icon class="loading-icon"><Loading /></el-icon>
            <div>加载中...</div>
          </div>
        </div>
        
        <div class="preview-controls">
          <div class="scale-controls">
            <el-button-group>
              <el-button
                size="small"
                @click="zoomOut"
                :disabled="imgScale <= 0.5"
                :icon="ZoomOut"
              >
                缩小
              </el-button>
              <el-button
                size="small"
                @click="resetZoom"
                :icon="Refresh"
              >
                重置 ({{ Math.round(imgScale * 100) }}%)
              </el-button>
              <el-button
                size="small"
                @click="zoomIn"
                :disabled="imgScale >= 3"
                :icon="ZoomIn"
              >
                放大
              </el-button>
            </el-button-group>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { 
  Search, 
  Refresh, 
  ZoomIn, 
  ZoomOut, 
  Check, 
  Close, 
  Picture, 
  Document, 
  Loading 
} from '@element-plus/icons-vue';
import request from "@/utils/request";
import { ElMessage, ElMessageBox } from "element-plus";

const rows = ref([]);
const preview = ref("");
const classOptions = ref([]);
const query = ref({ classId: undefined, dates: [] });
const imgScale = ref(1);
const loading = ref(false);
const dialogVisible = computed(() => !!preview.value);

// 待处理数量
const pendingCount = computed(() => {
  return rows.value.filter(item => item.approveStatus === 0).length;
});

const statusText = (v) => (v === 0 ? "待审批" : v === 1 ? "通过" : v === 2 ? "驳回" : String(v ?? ""));

const getStatusType = (status) => {
  switch(status) {
    case 0: return "warning";
    case 1: return "success";
    case 2: return "danger";
    default: return "info";
  }
};

const zoomIn = () => { 
  imgScale.value = Math.min(3, imgScale.value + 0.25); 
};
const zoomOut = () => { 
  imgScale.value = Math.max(0.5, imgScale.value - 0.25); 
};
const resetZoom = () => { 
  imgScale.value = 1; 
};
const closePreview = () => { 
  preview.value = ""; 
  imgScale.value = 1; 
};

// 鼠标滚轮缩放
const handleWheel = (event) => {
  event.preventDefault();
  if (event.deltaY < 0) {
    zoomIn();
  } else {
    zoomOut();
  }
};

// 图片加载处理
const handleImageLoad = () => {
  // 图片加载完成后的处理
};

const loadClasses = async () => {
  try {
    const res = await request.get("/api/counselor/check/classes");
    classOptions.value = res.data || [];
  } catch (error) {
    console.error("加载班级列表失败:", error);
    ElMessage.error("加载班级列表失败");
  }
};

const load = async () => {
  loading.value = true;
  try {
    const params = {
      current: 1,
      size: 30,
      approveStatus: 0,
      classId: query.value.classId || undefined,
      startDate: query.value.dates?.[0] || undefined,
      endDate: query.value.dates?.[1] || undefined
    };
    const res = await request.get("/api/counselor/leave/page", { params });
    rows.value = res.data?.records || [];
  } catch (error) {
    console.error("加载请假记录失败:", error);
    ElMessage.error("加载请假记录失败");
    rows.value = [];
  } finally {
    loading.value = false;
  }
};

const resetQuery = () => {
  query.value = { classId: undefined, dates: [] };
  load();
};

const approve = async (id, status) => {
  try {
    await ElMessageBox.confirm(
      status === 1 ? "确定要通过此请假申请吗？" : "确定要驳回此请假申请吗？",
      "确认操作",
      {
        confirmButtonText: "确认",
        cancelButtonText: "取消",
        type: status === 1 ? "success" : "warning",
        confirmButtonClass: status === 1 ? "confirm-approve" : "confirm-reject"
      }
    );
    
    await request.post(`/api/counselor/leave/approve/${id}`, {
      approveStatus: status,
      approveRemark: status === 1 ? "同意" : "请补充材料"
    });
    
    ElMessage.success(status === 1 ? "请假已通过" : "请假已驳回");
    load();
  } catch (error) {
    if (error !== 'cancel') {
      console.error("审批失败:", error);
      ElMessage.error("操作失败");
    }
  }
};

onMounted(() => {
  loadClasses();
  load();
});
</script>

<style scoped lang="less">
.leave-approval-container {
  padding: 20px;
  min-height: calc(100vh - 140px);
  
  .approval-card {
    border: none;
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
    
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 8px 16px;
      border-bottom: 1px solid #f0f0f0;
      
      .header-left {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .card-title {
          font-size: 20px;
          font-weight: 700;
          color: #1e293b;
          letter-spacing: -0.025em;
        }
        
        .pending-count {
          border-radius: 12px;
          font-weight: 600;
          padding: 4px 12px;
          background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
          border-color: #fbbf24;
          color: #92400e;
        }
      }
      
      .header-right {
        display: flex;
        gap: 12px;
      }
    }
  }
  
  .filter-container {
    margin-bottom: 24px;
    padding: 0 8px;
    
    .filter-row {
      display: flex;
      align-items: center;
      gap: 24px;
      flex-wrap: wrap;
      
      .filter-group {
        display: flex;
        align-items: center;
        gap: 24px;
        flex-wrap: wrap;
        
        .filter-item {
          display: flex;
          flex-direction: column;
          gap: 8px;
          min-width: 200px;
          
          .filter-label {
            font-size: 13px;
            color: #64748b;
            font-weight: 600;
            letter-spacing: 0.5px;
          }
          
          .filter-select {
            :deep(.el-input__wrapper) {
              border-radius: 12px;
              border: 1px solid #e2e8f0;
              background: #ffffff;
              box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
              transition: all 0.3s ease;
              
              &:hover {
                border-color: #cbd5e1;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
              }
              
              &.is-focus {
                border-color: #3b82f6;
                box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
              }
            }
          }
          
          .filter-date {
            width: 380px;
            
            :deep(.el-input__wrapper) {
              border-radius: 12px;
              border: 1px solid #e2e8f0;
              background: #ffffff;
              box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
              transition: all 0.3s ease;
              
              &:hover {
                border-color: #cbd5e1;
              }
              
              &.is-focus {
                border-color: #3b82f6;
                box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
              }
            }
          }
        }
      }
    }
  }
  
  .table-container {
    border-radius: 12px;
    overflow: hidden;
    border: 1px solid #e2e8f0;
    background: #ffffff;
    min-height: 400px;
    
    :deep(.table-header) {
      th {
        background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
        color: #334155;
        font-weight: 700;
        height: 56px;
        border-bottom: 2px solid #e2e8f0;
        letter-spacing: -0.01em;
        
        &:first-child {
          border-top-left-radius: 12px;
        }
        
        &:last-child {
          border-top-right-radius: 12px;
        }
      }
    }
    
    :deep(.el-table__row) {
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      
      &:hover {
        background: linear-gradient(90deg, #f8fafc 0%, #f0f9ff 100%) !important;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
        
        td {
          &:first-child {
            border-left: 4px solid #3b82f6;
            padding-left: 12px;
          }
        }
      }
      
      td {
        padding: 20px 0;
        border-bottom: 1px solid #f1f5f9;
        font-weight: 500;
        color: #475569;
      }
    }
    
    .apply-id {
      font-family: 'SF Mono', Monaco, 'Courier New', monospace;
      color: #3b82f6;
      font-weight: 700;
      font-size: 15px;
      letter-spacing: 0.5px;
    }
    
    .student-info {
      display: flex;
      flex-direction: column;
      gap: 6px;
      
      .student-name {
        font-weight: 600;
        color: #1e293b;
      }
      
      .class-tag {
        align-self: flex-start;
        border-radius: 8px;
        font-weight: 500;
        padding: 2px 8px;
        background: #f1f5f9;
        border-color: #cbd5e1;
        color: #475569;
      }
    }
    
    .reason-content {
      line-height: 1.5;
      color: #475569;
      max-height: 60px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
    
    .status-tag {
      border-radius: 12px;
      font-weight: 600;
      padding: 4px 12px;
      min-width: 60px;
      text-align: center;
      
      &.el-tag--warning {
        background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
        border-color: #fbbf24;
        color: #92400e;
      }
      
      &.el-tag--success {
        background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
        border-color: #10b981;
        color: #065f46;
      }
      
      &.el-tag--danger {
        background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
        border-color: #f87171;
        color: #991b1b;
      }
    }
    
    .proof-wrap {
      display: flex;
      align-items: center;
      justify-content: center;
      
      .image-preview {
        position: relative;
        width: 64px;
        height: 64px;
        border-radius: 12px;
        overflow: hidden;
        cursor: pointer;
        transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        
        &:hover {
          transform: translateY(-4px) scale(1.05);
          box-shadow: 0 8px 24px rgba(59, 130, 246, 0.2);
          
          .image-overlay {
            opacity: 1;
          }
        }
        
        .thumb-img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          display: block;
          transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        }
        
        .image-overlay {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background: rgba(59, 130, 246, 0.9);
          display: flex;
          align-items: center;
          justify-content: center;
          opacity: 0;
          transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          
          .el-icon {
            font-size: 20px;
            color: white;
          }
        }
      }
      
      .no-image {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 6px;
        color: #94a3b8;
        font-size: 12px;
        font-weight: 500;
        
        .el-icon {
          font-size: 24px;
          color: #cbd5e1;
        }
      }
    }
    
    .action-buttons {
      display: flex;
      gap: 8px;
      justify-content: center;
      
      .approve-btn {
        border-radius: 8px;
        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        border: none;
        font-weight: 600;
        padding: 8px 16px;
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
        }
      }
      
      .reject-btn {
        border-radius: 8px;
        background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
        border: none;
        font-weight: 600;
        padding: 8px 16px;
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
        }
      }
    }
  }
  
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    text-align: center;
    color: #94a3b8;
    
    .empty-icon {
      font-size: 48px;
      color: #cbd5e1;
      margin-bottom: 16px;
    }
    
    .empty-text {
      font-size: 18px;
      font-weight: 600;
      color: #64748b;
      margin-bottom: 8px;
    }
    
    .empty-subtext {
      font-size: 14px;
      color: #94a3b8;
    }
  }
}

.preview-dialog {
  :deep(.el-dialog) {
    border-radius: 16px;
    overflow: hidden;
  }
  
  :deep(.el-dialog__header) {
    padding: 20px 24px;
    border-bottom: 1px solid #f1f5f9;
    margin-right: 0;
    
    .el-dialog__title {
      font-weight: 700;
      color: #1e293b;
    }
  }
  
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.preview-container {
  display: flex;
  flex-direction: column;
  height: 600px;
  
  .image-viewer {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: auto;
    background: #f8fafc;
    cursor: grab;
    
    &:active {
      cursor: grabbing;
    }
    
    .preview-image {
      max-width: 100%;
      max-height: 100%;
      object-fit: contain;
      transition: transform 0.2s ease;
      border-radius: 8px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    }
    
    .image-loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 16px;
      color: #64748b;
      font-weight: 500;
      
      .loading-icon {
        font-size: 32px;
        animation: spin 1s linear infinite;
      }
    }
  }
  
  .preview-controls {
    padding: 20px 24px;
    background: #ffffff;
    border-top: 1px solid #f1f5f9;
    
    .scale-controls {
      display: flex;
      justify-content: center;
      
      :deep(.el-button-group) {
        .el-button {
          border-radius: 8px;
          font-weight: 600;
          padding: 8px 20px;
          transition: all 0.3s ease;
          
          &:hover {
            transform: translateY(-1px);
          }
          
          &:first-child {
            border-top-right-radius: 0;
            border-bottom-right-radius: 0;
          }
          
          &:last-child {
            border-top-left-radius: 0;
            border-bottom-left-radius: 0;
          }
        }
      }
    }
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .leave-approval-container {
    padding: 12px;
    
    .filter-row {
      flex-direction: column;
      align-items: stretch !important;
      gap: 16px !important;
      
      .filter-item {
        min-width: 100% !important;
      }
    }
    
    .card-header {
      flex-direction: column;
      align-items: stretch;
      gap: 12px;
      
      .header-left {
        justify-content: space-between;
      }
      
      .header-right {
        justify-content: center;
      }
    }
    
    .action-buttons {
      flex-direction: column;
      gap: 8px;
      
      .el-button {
        width: 100%;
      }
    }
  }
}
</style>