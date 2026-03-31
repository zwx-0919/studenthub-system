<template>
  <div class="forum-page">
    <!-- 工具栏 -->
    <div class="forum-toolbar">
      <div class="toolbar-header">
        <h2>📢 校园论坛</h2>
        <p>与同学们分享学习、生活中的点滴</p>
      </div>
      
      <div class="toolbar-actions compact">
        <div class="search-box compact">
          <svg class="search-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          </svg>
          <input 
            v-model="keyword" 
            class="search-input compact" 
            placeholder="搜索帖子内容或标题..." 
            @input="handleSearch"
          />
        </div>
        
        <div class="category-filter compact">
          <div class="filter-label">📁 筛选</div>
          <div class="filter-options">
            <div 
              v-for="c in categoryOptions" 
              :key="c"
              class="category-option compact"
              :class="{ 
                'active': checkedCategories.includes(c),
                'all': c === '全部' && checkedCategories.length === categoryOptions.length
              }"
              @click="toggleCategory(c)"
            >
              {{ c }}
            </div>
          </div>
        </div>
        
        <div class="toolbar-buttons">
          <button class="refresh-btn compact" @click="loadPosts" title="刷新">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="23 4 23 10 17 10"></polyline>
              <polyline points="1 20 1 14 7 14"></polyline>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
            </svg>
            刷新
          </button>
          <button class="publish-btn compact" @click="openPublishModal">
            <svg class="publish-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
            发布新帖
          </button>
        </div>
      </div>
    </div>

    <!-- 帖子列表 -->
    <div class="posts-container">
      <div v-if="filteredPosts.length > 0" class="posts-list">
        <div 
          v-for="post in filteredPosts" 
          :key="post.id" 
          class="post-card single-row"
          @click="goDetail(post)"
        >
          <!-- 帖子头部 -->
          <div class="post-header" @click.stop="post.userId && goUserHome(post)">
            <div class="user-avatar">
              <div class="avatar-circle">{{ post.userName.slice(0, 1) }}</div>
            </div>
            <div class="user-info">
              <div class="user-name">{{ post.userName }}</div>
              <div class="post-meta">
                <span class="post-time">⏰ {{ post.time }}</span>
                <span class="post-category" :class="getCategoryClass(post.category)">
                  {{ post.category }}
                </span>
              </div>
            </div>
          </div>
          
          <!-- 帖子内容 -->
          <div class="post-content">
            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-desc">{{ post.content }}</p>
            
            <!-- 图片预览 -->
            <div class="post-images" v-if="post.imageList?.length" @click.stop>
              <div 
                v-for="(img, idx) in post.imageList.slice(0, 3)" 
                :key="img + idx"
                class="image-preview"
                :class="{ 'more': idx === 2 && post.imageList.length > 3 }"
                @click="openImagePreview(post.imageList, idx)"
              >
                <el-image
                  class="post-image"
                  :src="img"
                  fit="cover"
                  lazy
                />
                <div v-if="idx === 2 && post.imageList.length > 3" class="image-count">
                  +{{ post.imageList.length - 3 }}
                </div>
              </div>
            </div>
          </div>
          
          <!-- 帖子操作 -->
          <div class="post-actions" @click.stop>
            <button 
              class="action-btn like-btn" 
              :class="{ 'liked': post.liked }"
              @click.stop="doLike(post)"
            >
              <svg v-if="post.liked" class="like-icon" viewBox="0 0 24 24" width="16" height="16" fill="currentColor" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
              </svg>
              <svg v-else class="like-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
              </svg>
              点赞
              <span v-if="post.likeCount > 0" class="count">{{ post.likeCount }}</span>
            </button>
            
            <button class="action-btn comment-btn" @click.stop="goDetail(post)">
              <svg class="comment-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v10z"></path>
              </svg>
              评论
            </button>
            
            <button 
              v-if="post.userId === store.user?.id" 
              class="action-btn delete-btn" 
              @click.stop="deleteMine(post)"
            >
              <svg class="delete-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                <line x1="10" y1="11" x2="10" y2="17"></line>
                <line x1="14" y1="11" x2="14" y2="17"></line>
              </svg>
              删除
            </button>
          </div>
        </div>
      </div>
      
      <div v-else class="empty-state">
        <div class="empty-icon">📝</div>
        <h3>暂无帖子</h3>
        <p>当前没有符合条件的帖子，尝试发布第一条吧！</p>
        <button class="empty-publish-btn" @click="openPublishModal">
          发布第一条帖子
        </button>
      </div>
    </div>

    <!-- 发布模态框 -->
    <div v-if="showPublishModal" class="modal-overlay" @click.self="closePublishModal">
      <div class="publish-modal">
        <div class="modal-header">
          <h3>✍️ 发布新帖</h3>
          <button class="close-btn" @click="closePublishModal">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        
        <div class="modal-content">
          <div class="form-group">
            <label class="form-label">📁 选择分类</label>
            <div class="select-wrapper">
              <select v-model="publishForm.category" class="category-select">
                <option v-for="c in categoryOptions" :key="c" :value="c">{{ c }}</option>
              </select>
              <div class="select-arrow">▼</div>
            </div>
          </div>
          
          <div class="form-group">
            <label class="form-label">📝 帖子标题</label>
            <input 
              v-model.trim="publishForm.title" 
              class="form-input" 
              placeholder="请输入标题（5-30字）" 
              maxlength="30"
            />
            <div class="input-count">{{ publishForm.title.length }}/30</div>
          </div>
          
          <div class="form-group">
            <label class="form-label">📄 帖子内容</label>
            <textarea 
              v-model.trim="publishForm.content" 
              class="form-textarea" 
              placeholder="请输入内容，分享你的想法（10-300字）" 
              rows="5"
              maxlength="300"
            />
            <div class="textarea-count">{{ publishForm.content.length }}/300</div>
          </div>
          
          <div class="form-group">
            <label class="form-label">🖼️ 上传图片（可选）</label>
            <div class="upload-hint">支持 JPG/PNG/WebP/GIF 格式，单张不超过 5MB，最多 9 张</div>
            
            <div class="upload-area" @click="triggerFileInput">
              <input 
                ref="fileInput"
                type="file" 
                accept="image/*" 
                multiple 
                @change="handleImageChange"
                style="display: none"
              />
              <div class="upload-placeholder">
                <svg class="upload-icon" viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="17 8 12 3 7 8"></polyline>
                  <line x1="12" y1="3" x2="12" y2="15"></line>
                </svg>
                <p>点击或拖拽图片到此区域</p>
                <p class="upload-sub">支持多选，最多 9 张</p>
              </div>
            </div>
            
            <div v-if="uploading" class="uploading-indicator">
              <div class="uploading-spinner"></div>
              <span>图片上传中...</span>
            </div>
            
            <div v-if="uploadItems.length > 0" class="upload-preview">
              <div class="preview-grid">
                <div 
                  v-for="item in uploadItems" 
                  :key="item.id"
                  class="preview-item"
                  :class="item.status"
                >
                  <div class="preview-image" @click.stop>
                    <el-image
                      v-if="item.url"
                      class="preview-img"
                      :src="item.url"
                      :preview-src-list="publishImageList"
                      fit="cover"
                      preview-teleported
                    />
                    <div v-else class="preview-placeholder">
                      <div class="preview-progress">
                        <div class="progress-circle" :style="{ background: `conic-gradient(#6a55ff ${item.progress}%, transparent ${item.progress}%)` }"></div>
                        <span class="progress-text">{{ item.progress }}%</span>
                      </div>
                    </div>
                    
                    <div class="preview-overlay">
                      <button 
                        v-if="item.status === 'error'"
                        class="overlay-btn retry-btn"
                        @click.stop="retryUpload(item)"
                      >
                        重试
                      </button>
                      <button 
                        class="overlay-btn remove-btn"
                        @click.stop="removeImage(item.id)"
                      >
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                          <line x1="18" y1="6" x2="6" y2="18"></line>
                          <line x1="6" y1="6" x2="18" y2="18"></line>
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div v-if="publishError" class="error-message">
            <svg class="error-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            {{ publishError }}
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="cancel-btn" @click="closePublishModal">取消</button>
          <button 
            class="publish-submit-btn" 
            @click="handlePublish"
            :disabled="uploading || !canPublish"
            :class="{ 'disabled': uploading || !canPublish }"
          >
            <svg v-if="!uploading" class="submit-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 2L11 13"></path>
              <path d="M22 2l-7 20-4-9-9-4 20-7z"></path>
            </svg>
            <div v-else class="spinner"></div>
            {{ uploading ? '发布中...' : '发布帖子' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, nextTick } from "vue";
import { useRouter } from "vue-router";
import request from "@/utils/request";
import { useUserStore } from "@/store/user";
import { ElMessage, ElMessageBox } from "element-plus";

const store = useUserStore();
const router = useRouter();
const fileInput = ref(null);

const categoryOptions = ["求助", "二手", "失物", "其他"];
const checkedCategories = ref([...categoryOptions]);
const keyword = ref("");
const showPublishModal = ref(false);
const publishError = ref("");
const publishForm = ref({
  category: categoryOptions[0],
  title: "",
  content: "",
  imageUrl: ""
});
const publishImageList = ref([]);
const uploading = ref(false);
const uploadItems = ref([]);
let uploadSeq = 1;

const posts = ref([
  {
    id: 1,
    userName: "张同学",
    time: "刚刚",
    category: "二手",
    title: "二手高数教材转让",
    content: "九成新，同校面交。",
    likeCount: 5,
    liked: false
  },
  {
    id: 2,
    userName: "李同学",
    time: "10分钟前",
    category: "求助",
    title: "求问离散数学复习重点",
    content: "有没有学长学姐分享下重点章节。",
    likeCount: 12,
    liked: true
  }
]);

// 计算属性
const canPublish = computed(() => {
  return publishForm.value.title.length >= 5 && 
         publishForm.value.title.length <= 30 &&
         publishForm.value.content.length >= 10 && 
         publishForm.value.content.length <= 300;
});

const filteredPosts = computed(() =>
  posts.value.filter(
    (p) =>
      checkedCategories.value.includes(p.category) &&
      (!keyword.value ||
        p.title.includes(keyword.value) ||
        p.content.includes(keyword.value))
  )
);

// 方法
const formatTime = (raw) => {
  if (!raw) return "刚刚";
  return String(raw).replace("T", " ").slice(0, 16);
};

const parseImageList = (imageUrl) => {
  if (!imageUrl) return [];
  return String(imageUrl)
    .split(",")
    .map((x) => x.trim())
    .filter(Boolean);
};

const mapPost = (p) => ({
  id: p.id,
  userId: p.userId,
  userName: p.userName || "匿名用户",
  time: formatTime(p.createTime),
  category: p.category || "其他",
  title: p.title || "",
  content: p.content || "",
  imageUrl: p.imageUrl || "",
  imageList: parseImageList(p.imageUrl),
  likeCount: p.likeCount || 0,
  liked: p.liked || false
});

const getCategoryClass = (category) => {
  const classes = {
    "求助": "help",
    "二手": "trade",
    "失物": "lost",
    "其他": "other"
  };
  return classes[category] || "other";
};

const toggleCategory = (category) => {
  const index = checkedCategories.value.indexOf(category);
  if (index > -1) {
    checkedCategories.value.splice(index, 1);
  } else {
    checkedCategories.value.push(category);
  }
};

const handleSearch = () => {
  // 搜索逻辑，可以添加防抖
};

const resetPublishForm = () => {
  publishForm.value = {
    category: categoryOptions[0],
    title: "",
    content: "",
    imageUrl: ""
  };
  publishImageList.value = [];
  uploadItems.value = [];
  publishError.value = "";
  uploading.value = false;
};

const openPublishModal = () => {
  showPublishModal.value = true;
  publishError.value = "";
  nextTick(() => {
    if (fileInput.value) {
      fileInput.value.value = "";
    }
  });
};

const closePublishModal = () => {
  showPublishModal.value = false;
  resetPublishForm();
};

const triggerFileInput = () => {
  fileInput.value?.click();
};

const validatePublishForm = () => {
  if (!publishForm.value.title || publishForm.value.title.length < 5) {
    return "标题至少 5 个字";
  }
  if (!publishForm.value.content || publishForm.value.content.length < 10) {
    return "内容至少 10 个字";
  }
  return "";
};

const createPostRemote = async (payload) => {
  const res = await request.post("/api/student/post/add", payload);
  return mapPost(res.data);
};

const loadPosts = async () => {
  try {
    const res = await request.get("/api/student/post/page", {
      params: { current: 1, size: 50 }
    });
    const records = res.data?.records || [];
    if (records.length) {
      posts.value = records.map(mapPost);
    }
  } catch (_) {
    // ignore, keep local mock data
  }
};

const syncImageUrl = () => {
  publishImageList.value = uploadItems.value
    .filter((x) => x.status === "success" && x.url)
    .map((x) => x.url);
  publishForm.value.imageUrl = publishImageList.value.join(",");
};

const uploadSingle = async (item) => {
  item.status = "uploading";
  item.progress = 0;
  const formData = new FormData();
  formData.append("file", item.file);
  const res = await request.post("/api/student/post/upload-image", formData, {
    headers: { "Content-Type": "multipart/form-data" },
    onUploadProgress: (evt) => {
      const total = evt?.total || 0;
      if (total > 0) {
        item.progress = Math.min(99, Math.round((evt.loaded / total) * 100));
      }
    }
  });
  item.url = res.data || "";
  item.status = "success";
  item.progress = 100;
  syncImageUrl();
};

const handleImageChange = async (e) => {
  const files = Array.from(e?.target?.files || []);
  if (!files.length) return;
  const remain = Math.max(0, 9 - uploadItems.value.length);
  if (remain <= 0) {
    ElMessage.warning("最多上传9张图片");
    e.target.value = "";
    return;
  }
  const picked = files.slice(0, remain);
  if (files.length > remain) {
    ElMessage.warning(`最多上传9张图片，已截取前 ${remain} 张`);
  }

  const newItems = picked.map((file) => ({
    id: uploadSeq++,
    file,
    url: "",
    progress: 0,
    status: "pending"
  }));
  uploadItems.value.push(...newItems);

  uploading.value = true;
  await Promise.all(
    newItems.map(async (item) => {
      try {
        await uploadSingle(item);
      } catch (err) {
        item.status = "error";
        item.progress = 0;
        ElMessage.error(err?.message || "图片上传失败");
      }
    })
  );
  uploading.value = uploadItems.value.some((x) => x.status === "uploading");
  e.target.value = "";
};

const retryUpload = async (item) => {
  if (!item?.file) return;
  try {
    uploading.value = true;
    await uploadSingle(item);
  } catch (err) {
    item.status = "error";
    item.progress = 0;
    ElMessage.error(err?.message || "重试失败");
  } finally {
    uploading.value = uploadItems.value.some((x) => x.status === "uploading");
  }
};

const removeImage = (id) => {
  uploadItems.value = uploadItems.value.filter((x) => x.id !== id);
  syncImageUrl();
};

const openImagePreview = (images, index) => {
  // 这里可以触发element-plus的图片预览
  // 由于预览功能已集成在el-image中，此处可以留空或添加自定义逻辑
};

const handlePublish = async () => {
  if (uploadItems.value.some((x) => x.status === "uploading")) {
    publishError.value = "图片上传中，请稍后发布";
    return;
  }
  const err = validatePublishForm();
  if (err) {
    publishError.value = err;
    return;
  }
  try {
    const created = await createPostRemote({
      category: publishForm.value.category,
      title: publishForm.value.title,
      content: publishForm.value.content,
      imageUrl: publishForm.value.imageUrl
    });
    posts.value.unshift(created);
    closePublishModal();
    ElMessage.success("发布成功");
  } catch (e) {
    const msg = e?.message || "发布失败，请稍后重试";
    ElMessage.error(msg.includes("禁言") ? "您已被禁言，无法发布内容" : msg);
  }
};

const doLike = async (post) => {
  if (!post.userId || post.userId === store.user?.id) return;
  try {
    await request.post(`/api/student/post/like/${post.id}`);
    post.likeCount = (post.likeCount || 0) + 1;
    post.liked = true;
    ElMessage.success("点赞成功");
  } catch (_) {
    // ignore
  }
};

const goDetail = (post) => router.push(`/student/post/${post.id}`);

const goUserHome = (post) => {
  if (post?.userId) router.push(`/student/user/${post.userId}`);
};

const deleteMine = async (post) => {
  try {
    await ElMessageBox.confirm("确定删除你发布的这条帖子吗？", "提示", { 
      type: "warning",
      confirmButtonText: "确认删除",
      cancelButtonText: "取消"
    });
    await request.delete(`/api/student/post/${post.id}`);
    posts.value = posts.value.filter((x) => x.id !== post.id);
    ElMessage.success("删除成功");
  } catch (error) {
    // 用户取消删除
  }
};

onMounted(loadPosts);
</script>

<style scoped lang="less">
.forum-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
  background: #f8f9ff;
  min-height: 100vh;
}

/* 工具栏样式 */
.forum-toolbar {
  background: white;
  border-radius: 20px;
  padding: 24px 32px;
  margin: 20px 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
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

.toolbar-header {
  margin-bottom: 20px;
}

.toolbar-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px;
  letter-spacing: -0.3px;
}

.toolbar-header p {
  font-size: 14px;
  color: #666;
  margin: 0;
  line-height: 1.6;
  opacity: 0.9;
}

.toolbar-actions.compact {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-box.compact {
  position: relative;
  max-width: 400px;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  stroke-width: 2.5;
}

.search-input.compact {
  width: 100%;
  height: 40px;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 0 20px 0 44px;
  font-size: 14px;
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

.category-filter.compact {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-label {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a2e;
  white-space: nowrap;
}

.filter-options {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.category-option.compact {
  padding: 6px 12px;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: white;
  color: #666;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  
  &:hover {
    background: rgba(106, 85, 255, 0.08);
    color: #6a55ff;
  }
  
  &.active {
    background: rgba(106, 85, 255, 0.15);
    color: #6a55ff;
    border-color: rgba(106, 85, 255, 0.3);
  }
  
  &.all {
    background: linear-gradient(135deg, rgba(106, 85, 255, 0.1), rgba(138, 122, 255, 0.1));
    border-color: rgba(106, 85, 255, 0.2);
  }
}

.toolbar-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.refresh-btn.compact,
.publish-btn.compact {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.refresh-btn.compact {
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

.publish-btn.compact {
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  box-shadow: 0 4px 12px rgba(106, 85, 255, 0.3);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(106, 85, 255, 0.4);
  }
  
  .publish-icon {
    stroke: white;
  }
}

/* 帖子列表样式 */
.posts-container {
  background: white;
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
  min-height: 400px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card.single-row {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(106, 85, 255, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100%;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(106, 85, 255, 0.12);
    border-color: rgba(106, 85, 255, 0.2);
  }
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #6a55ff, #8a7aff);
    opacity: 0;
    transition: opacity 0.3s ease;
  }
  
  &:hover::before {
    opacity: 1;
  }
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  cursor: pointer;
  
  &:hover .user-name {
    color: #6a55ff;
  }
}

.avatar-circle {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(106, 85, 255, 0.3);
  transition: all 0.3s ease;
  
  .post-header:hover & {
    transform: scale(1.05);
  }
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 4px;
  transition: color 0.2s ease;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.post-time {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-category {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 8px;
  font-weight: 500;
  white-space: nowrap;
  
  &.help {
    background: rgba(255, 107, 107, 0.1);
    color: #ff6b6b;
    border: 1px solid rgba(255, 107, 107, 0.2);
  }
  
  &.trade {
    background: rgba(78, 205, 196, 0.1);
    color: #4ecdc4;
    border: 1px solid rgba(78, 205, 196, 0.2);
  }
  
  &.lost {
    background: rgba(255, 230, 109, 0.1);
    color: #d4a017;
    border: 1px solid rgba(255, 230, 109, 0.2);
  }
  
  &.other {
    background: rgba(138, 122, 255, 0.1);
    color: #8a7aff;
    border: 1px solid rgba(138, 122, 255, 0.2);
  }
}

.post-content {
  margin-bottom: 20px;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-desc {
  font-size: 14px;
  line-height: 1.6;
  color: #666;
  margin: 0 0 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  max-width: 300px;
  overflow: hidden;
}

.image-preview {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  aspect-ratio: 1;
  background: #f5f5f5;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: scale(1.05);
  }
  
  &.more {
    &::after {
      content: '';
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

.post-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-count {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 18px;
  font-weight: 600;
  z-index: 1;
}

.post-actions {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: white;
  color: #666;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  flex: 1;
  justify-content: center;
  
  &:hover {
    transform: translateY(-1px);
  }
  
  svg {
    stroke: currentColor;
  }
}

.like-btn {
  &:hover {
    background: rgba(255, 107, 107, 0.08);
    color: #ff6b6b;
    border-color: rgba(255, 107, 107, 0.2);
  }
  
  &.liked {
    background: rgba(255, 107, 107, 0.15);
    color: #ff6b6b;
    border-color: rgba(255, 107, 107, 0.3);
    
    .like-icon {
      fill: #ff6b6b;
    }
  }
  
  .count {
    font-weight: 600;
  }
}

.comment-btn {
  &:hover {
    background: rgba(106, 85, 255, 0.08);
    color: #6a55ff;
    border-color: rgba(106, 85, 255, 0.2);
  }
}

.delete-btn {
  &:hover {
    background: rgba(255, 107, 107, 0.08);
    color: #ff6b6b;
    border-color: rgba(255, 107, 107, 0.2);
  }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
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
  margin: 0 0 24px;
  line-height: 1.6;
  max-width: 400px;
  margin: 0 auto 24px;
}

.empty-publish-btn {
  padding: 12px 32px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(106, 85, 255, 0.3);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(106, 85, 255, 0.4);
  }
}

/* 发布模态框 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
  padding: 20px;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.publish-modal {
  background: white;
  border-radius: 20px;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
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

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  background: white;
  z-index: 1;
  border-radius: 20px 20px 0 0;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.close-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: white;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  
  &:hover {
    background: rgba(106, 85, 255, 0.08);
    color: #6a55ff;
  }
  
  svg {
    stroke: currentColor;
  }
}

.modal-content {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
  position: relative;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 下拉框样式 */
.select-wrapper {
  position: relative;
  width: 100%;
}

.category-select {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 0 16px;
  font-size: 14px;
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

.form-input,
.form-textarea {
  width: 100%;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 10px 16px;
  font-size: 14px;
  font-family: inherit;
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

.form-input {
  height: 44px;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
  line-height: 1.6;
}

.input-count,
.textarea-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.upload-hint {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
  line-height: 1.5;
}

.upload-area {
  border: 2px dashed rgba(106, 85, 255, 0.3);
  border-radius: 12px;
  padding: 30px 20px;
  text-align: center;
  background: rgba(248, 247, 255, 0.3);
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 12px;
  
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
}

.upload-icon {
  stroke-width: 1.5;
  margin-bottom: 4px;
}

.upload-sub {
  font-size: 12px;
  color: #999;
}

.uploading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: rgba(106, 85, 255, 0.08);
  border-radius: 10px;
  border: 1px solid rgba(106, 85, 255, 0.2);
  color: #6a55ff;
  font-size: 13px;
  margin-bottom: 12px;
}

.uploading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(106, 85, 255, 0.3);
  border-top-color: #6a55ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.upload-preview {
  margin-top: 12px;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 10px;
}

.preview-item {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  aspect-ratio: 1;
  background: #f5f5f5;
  
  &.error {
    &::before {
      content: '上传失败';
      position: absolute;
      inset: 0;
      background: rgba(255, 107, 107, 0.8);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 11px;
      font-weight: 500;
      z-index: 1;
    }
  }
}

.preview-image {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(106, 85, 255, 0.1), rgba(138, 122, 255, 0.1));
}

.preview-progress {
  position: relative;
  width: 50px;
  height: 50px;
}

.progress-circle {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  position: relative;
  z-index: 1;
  
  &::before {
    content: '';
    position: absolute;
    inset: 4px;
    background: white;
    border-radius: 50%;
    z-index: 2;
  }
}

.progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 11px;
  font-weight: 600;
  color: #6a55ff;
  z-index: 3;
}

.preview-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  opacity: 0;
  transition: opacity 0.3s ease;
  
  .preview-item:hover & {
    opacity: 1;
  }
}

.overlay-btn {
  padding: 4px 8px;
  border-radius: 6px;
  border: none;
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 4px;
}

.retry-btn {
  background: white;
  color: #6a55ff;
  
  &:hover {
    background: #f5f5f5;
  }
}

.remove-btn {
  background: rgba(255, 107, 107, 0.9);
  color: white;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:hover {
    background: #ff6b6b;
  }
  
  svg {
    stroke: white;
  }
}

.error-message {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 12px;
  background: rgba(255, 107, 107, 0.08);
  border-radius: 10px;
  border: 1px solid rgba(255, 107, 107, 0.2);
  color: #ff6b6b;
  font-size: 13px;
  margin: 16px 0;
}

.error-icon {
  stroke: #ff6b6b;
  flex-shrink: 0;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  position: sticky;
  bottom: 0;
  background: white;
  border-radius: 0 0 20px 20px;
}

.cancel-btn,
.publish-submit-btn {
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 100px;
  justify-content: center;
}

.cancel-btn {
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.1);
  color: #666;
  
  &:hover {
    background: rgba(0, 0, 0, 0.05);
  }
}

.publish-submit-btn {
  background: linear-gradient(135deg, #6a55ff, #8a7aff);
  color: white;
  box-shadow: 0 4px 12px rgba(106, 85, 255, 0.3);
  
  &:hover:not(.disabled) {
    transform: translateY(-1px);
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
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .forum-page {
    padding: 0 16px 24px;
  }
  
  .forum-toolbar {
    padding: 20px;
    border-radius: 16px;
    margin: 16px 0;
  }
  
  .toolbar-header h2 {
    font-size: 20px;
  }
  
  .toolbar-header p {
    font-size: 13px;
  }
  
  .posts-container {
    padding: 20px;
    border-radius: 16px;
  }
  
  .post-card.single-row {
    padding: 16px;
  }
  
  .publish-modal {
    max-height: 95vh;
  }
  
  .modal-content {
    padding: 20px;
  }
  
  .modal-footer {
    padding: 16px 20px;
  }
}
</style>