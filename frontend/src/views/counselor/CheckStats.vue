<template>
  <div class="checkin-dashboard">
    <el-card v-if="warnings.length" class="warn-card" shadow="never">
      <template #header>
        <span class="warn-title">⚠️ 本周学业预警（打卡/学习时长未达标）</span>
      </template>
      <el-table :data="warnings" size="small" stripe>
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="className" label="班级" min-width="140" />
        <el-table-column prop="weeklyCheckCount" label="本周打卡次数" width="120" />
        <el-table-column prop="weeklyDurationMinutes" label="本周学习时长(分钟)" width="160" />
      </el-table>
    </el-card>

    <el-card class="overview-card" shadow="never">
      <template #header>
        <div class="overview-header">
          <div class="overview-title">学习打卡可视化概览</div>
          <div class="overview-meta">
            <span class="rate-label">当前筛选范围打卡率</span>
            <span class="rate-value">{{ formatPercent(overview?.checkRate ?? 0) }}</span>
          </div>
        </div>
      </template>
      <div class="overview-body">
        <div ref="trendRef" class="trend-chart"></div>
      </div>
    </el-card>

    <el-card class="main-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="card-title">打卡记录</span>
            <el-tag type="info" size="small" class="record-count">共 {{ rows.length }} 条记录</el-tag>
          </div>
        </div>
      </template>
      
      <div class="filter-container">
        <div class="filter-group">
          <el-select
            v-model="query.classId"
            placeholder="选择班级"
            clearable
            class="filter-item"
            style="width: 200px"
          >
            <el-option
              v-for="c in classOptions"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
          
          <el-date-picker
            v-model="query.dates"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            class="filter-item"
            style="width: 400px"
          />
          
          <el-button
            type="primary"
            @click="load"
            class="query-btn"
            :icon="Search"
          >
            查询
          </el-button>
          <el-button
            @click="resetQuery"
            class="reset-btn"
          >
            重置
          </el-button>
        </div>
      </div>

      <div class="table-container">
        <el-table
          :data="rows"
          style="width: 100%"
          stripe
          header-row-class-name="table-header"
          :border="true"
          v-loading="loading"
        >
          <el-table-column prop="studentName" label="学生姓名" width="120" fixed="left" />
          <el-table-column prop="className" label="班级" width="150" />
          <el-table-column prop="location" label="打卡位置" min-width="200" />
          <el-table-column label="打卡图片" width="120" align="center">
            <template #default="{ row }">
              <div class="image-cell">
                <div
                  v-if="row.imageUrl"
                  class="image-preview"
                  @click="handleImagePreview(row.imageUrl)"
                >
                  <img
                    :src="row.imageUrl"
                    class="thumb-img"
                    :alt="`${row.studentName}的打卡图片`"
                  />
                  <div class="image-overlay">
                    <el-icon><ZoomIn /></el-icon>
                  </div>
                </div>
                <span v-else class="empty-image">无图片</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="checkTime" label="打卡时间" width="180" />
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.imageUrl"
                type="primary"
                link
                @click="handleImagePreview(row.imageUrl)"
              >
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 图片预览对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="60%"
      align-center
      destroy-on-close
      @close="handleDialogClose"
    >
      <div class="image-viewer">
        <img
          v-if="currentImage"
          :src="currentImage"
          :key="currentImage"
          class="preview-image"
          @load="handleImageLoad"
        />
        <div v-else class="image-loading">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <div>加载中...</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, shallowRef, nextTick } from "vue";
import * as echarts from "echarts";
import { Search, ZoomIn, Loading } from '@element-plus/icons-vue';
import request from "@/utils/request";

const rows = ref([]);
const loading = ref(false);
const classOptions = ref([]);
const overview = ref(null);
const query = ref({
  classId: undefined,
  dates: []
});

const trendRef = ref(null);
const trendChart = shallowRef(null);

// 图片预览相关
const dialogVisible = ref(false);
const currentImage = ref("");
const dialogTitle = ref("打卡图片");
const imageLoaded = ref(false);

const loadClasses = async () => {
  try {
    const res = await request.get("/api/counselor/check/classes");
    classOptions.value = res.data || [];
  } catch (error) {
    console.error("加载班级列表失败:", error);
  }
};

const load = async () => {
  loading.value = true;
  try {
    const params = {
      current: 1,
      size: 30,
      classId: query.value.classId || undefined,
      startDate: query.value.dates?.[0] || undefined,
      endDate: query.value.dates?.[1] || undefined
    };
    const res = await request.get("/api/counselor/check/page", { params });
    rows.value = res.data?.records || [];
  } catch (error) {
    console.error("加载打卡记录失败:", error);
    rows.value = [];
  } finally {
    loading.value = false;
  }
};

const loadOverview = async () => {
  try {
    const params = {
      current: 1,
      size: 1,
      classId: query.value.classId || undefined,
      startDate: query.value.dates?.[0] || undefined,
      endDate: query.value.dates?.[1] || undefined
    };
    const res = await request.get("/api/counselor/check/overview", { params });
    overview.value = res.data || {};
    const last7 = overview.value.last7Days || {};
    if (trendChart.value) {
      trendChart.value.setOption({
        title: { text: "近7日打卡次数 & 学习时长", left: "center", textStyle: { fontSize: 13 } },
        tooltip: { trigger: "axis" },
        legend: { data: ["打卡次数", "学习时长(分钟)"], bottom: 0 },
        grid: { left: 40, right: 20, top: 40, bottom: 50 },
        xAxis: {
          type: "category",
          data: last7.labels || [],
          axisLabel: { fontSize: 11 }
        },
        yAxis: [
          { type: "value", name: "次数", minInterval: 1 },
          { type: "value", name: "分钟" }
        ],
        series: [
          {
            name: "打卡次数",
            type: "line",
            smooth: true,
            data: last7.checkCounts || [],
            itemStyle: { color: "#3b82f6" },
            areaStyle: { opacity: 0.15 }
          },
          {
            name: "学习时长(分钟)",
            type: "line",
            smooth: true,
            yAxisIndex: 1,
            data: last7.durationMinutes || [],
            itemStyle: { color: "#22c55e" },
            areaStyle: { opacity: 0.1 }
          }
        ]
      });
    }
  } catch (e) {
    console.error("加载打卡概览失败:", e);
  }
};

const resetQuery = () => {
  query.value = {
    classId: undefined,
    dates: []
  };
  load();
  loadOverview();
};

// 图片预览处理
const handleImagePreview = (imageUrl) => {
  if (!imageUrl) return;
  currentImage.value = imageUrl;
  dialogVisible.value = true;
  dialogTitle.value = "打卡图片预览";
  imageLoaded.value = false;
};

// 图片加载完成
const handleImageLoad = () => {
  imageLoaded.value = true;
};

// 对话框关闭
const handleDialogClose = () => {
  // 延迟清空，避免闪屏
  setTimeout(() => {
    currentImage.value = "";
    dialogVisible.value = false;
  }, 300);
};

const warnings = ref([]);

const loadWarnings = async () => {
  try {
    const res = await request.get("/api/counselor/check/warnings");
    warnings.value = res.data || [];
  } catch (_) {
    warnings.value = [];
  }
};

const formatPercent = (value) => {
  const num = Number(value || 0);
  if (!Number.isFinite(num) || num <= 0) return "0%";
  // 后端返回的是 0-1 比例
  if (num <= 1) {
    return `${Math.round(num * 100)}%`;
  }
  return `${Math.round(num)}%`;
};

onMounted(async () => {
  await nextTick();
  if (trendRef.value) {
    trendChart.value = echarts.init(trendRef.value);
  }
  loadClasses();
  loadWarnings();
  load();
  loadOverview();
});
</script>

<style scoped lang="less">
.warn-card {
  margin-bottom: 16px;
  border-radius: 16px;
  border: 1px solid rgba(255, 180, 80, 0.4);
  .warn-title {
    font-weight: 600;
    color: #b45309;
  }
}

.checkin-dashboard {
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  min-height: calc(100vh - 64px);
  
  .overview-card {
    border: none;
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
    margin-bottom: 20px;
    
    .overview-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      
      .overview-title {
        font-size: 18px;
        font-weight: 700;
        color: #1e293b;
      }
      
      .overview-meta {
        display: flex;
        align-items: baseline;
        gap: 8px;
        
        .rate-label {
          font-size: 13px;
          color: #64748b;
        }
        
        .rate-value {
          font-size: 20px;
          font-weight: 700;
          color: #16a34a;
        }
      }
    }
    
    .overview-body {
      padding: 8px 0 4px;
      
      .trend-chart {
        width: 100%;
        height: 260px;
      }
    }
  }
  
  .main-card {
    border: none;
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
    
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 8px;
      
      .header-left {
        display: flex;
        align-items: center;
        gap: 12px;
      }
      
      .card-title {
        font-size: 20px;
        font-weight: 700;
        color: #1e293b;
        letter-spacing: -0.025em;
      }
      
      .record-count {
        border-radius: 12px;
        font-weight: 500;
        background: #f1f5f9;
        border-color: #e2e8f0;
        color: #64748b;
      }
    }
  }
  
  .filter-container {
    margin-bottom: 24px;
    padding: 0 8px;
    
    .filter-group {
      display: flex;
      align-items: center;
      gap: 16px;
      flex-wrap: wrap;
      
      .filter-item {
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        
        :deep(.el-input__wrapper) {
          border-radius: 12px;
          border: 1px solid #e2e8f0;
          background: #ffffff;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          
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
      
      .query-btn {
        border-radius: 12px;
        padding: 0 24px;
        height: 40px;
        font-weight: 600;
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        border: none;
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
        }
        
        &:active {
          transform: translateY(0);
        }
      }
      
      .reset-btn {
        border-radius: 12px;
        padding: 0 24px;
        height: 40px;
        border: 1px solid #cbd5e1;
        color: #64748b;
        background: #ffffff;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        
        &:hover {
          color: #3b82f6;
          border-color: #3b82f6;
          background: #f8fafc;
        }
      }
    }
  }
  
  .table-container {
    border-radius: 12px;
    overflow: hidden;
    border: 1px solid #e2e8f0;
    background: #ffffff;
    
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
  }
  
  .image-cell {
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
    
    .empty-image {
      color: #94a3b8;
      font-size: 13px;
      font-weight: 500;
    }
  }
  
  .image-viewer {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 400px;
    width: 100%;
    overflow: hidden;
    background: #f8fafc;
    border-radius: 12px;
    
    .preview-image {
      max-width: 100%;
      max-height: 70vh;
      object-fit: contain;
      border-radius: 8px;
      animation: fadeIn 0.3s ease-out;
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
  
  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: scale(0.95);
    }
    to {
      opacity: 1;
      transform: scale(1);
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
    padding: 12px;
    
    .filter-group {
      flex-direction: column;
      align-items: stretch;
      
      .filter-item,
      .query-btn,
      .reset-btn {
        width: 100%;
      }
    }
    
    .card-title {
      font-size: 18px;
    }
  }
}
</style>