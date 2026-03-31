<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside width="280px" class="sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <div class="logo-icon">🛡️</div>
          <div class="logo-text">
            <h2>Admin Center</h2>
            <p>管理系统</p>
          </div>
        </div>
      </div>
      
      <div class="sidebar-nav">
        <div class="nav-item" 
             v-for="item in menuItems" 
             :key="item.path"
             :class="{ 'active': $route.path.startsWith(item.path) }"
             @click="goTo(item.path)">
          <div class="nav-icon">{{ item.icon }}</div>
          <span class="nav-text">{{ item.text }}</span>
          <div class="nav-arrow">›</div>
        </div>
      </div>
      
      <div class="sidebar-footer">
        <div class="admin-info">
          <div class="admin-avatar">{{ store.user?.userName?.slice(0, 1) || "A" }}</div>
          <div class="admin-details">
            <div class="admin-name">{{ store.user?.userName || "管理员" }}</div>
            <div class="admin-role">系统管理员</div>
          </div>
        </div>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <div class="page-title">
            <h3>{{ getPageTitle() }}</h3>
            <p>{{ getPageDescription() }}</p>
          </div>
        </div>
        
        <div class="header-right">
          <div class="header-actions">
            <button class="action-btn refresh" @click="refreshPage" title="刷新">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="23 4 23 10 17 10"></polyline>
                <polyline points="1 20 1 14 7 14"></polyline>
                <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
              </svg>
            </button>
            
            <div class="header-back">
              <GlobalBackButton />
            </div>
            
            <button class="logout-btn" @click="logout">
              <svg class="logout-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                <polyline points="16 17 21 12 16 7"></polyline>
                <line x1="21" y1="12" x2="9" y2="12"></line>
              </svg>
              退出登录
            </button>
          </div>
        </div>
      </el-header>

      <!-- 主要内容 -->
      <el-main class="main-content">
        <div class="content-wrapper">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/store/user";
import GlobalBackButton from "@/components/GlobalBackButton.vue";

const router = useRouter();
const route = useRoute();
const store = useUserStore();

// 菜单项定义
const menuItems = ref([
  { icon: "📢", text: "帖子管理", path: "/admin/posts", description: "管理用户发布的帖子内容" },
  { icon: "📢", text: "公告管理", path: "/admin/notice", description: "发布和管理系统公告" },
  { icon: "👥", text: "用户管理", path: "/admin/users", description: "管理系统用户和权限" },
  { icon: "📚", text: "课程管理", path: "/admin/courses", description: "管理课程信息和安排" },
  { icon: "📝", text: "考试管理", path: "/admin/exams", description: "管理考试安排和成绩" }
]);

// 获取页面标题
const getPageTitle = () => {
  const item = menuItems.value.find(item => route.path.startsWith(item.path));
  return item?.text || "管理员工作台";
};

// 获取页面描述
const getPageDescription = () => {
  const item = menuItems.value.find(item => route.path.startsWith(item.path));
  return item?.description || "系统管理后台";
};

// 导航到指定页面
const goTo = (path) => {
  router.push(path);
};

// 刷新当前页面
const refreshPage = () => {
  router.go(0);
};

// 退出登录
const logout = () => {
  store.logout();
  router.replace("/login");
};
</script>

<style scoped lang="less">
.admin-layout {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 侧边栏样式 */
.sidebar {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: white;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.1);
  z-index: 100;
  position: relative;
  transition: all 0.3s ease;
}

.sidebar-header {
  padding: 32px 24px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: transform 0.2s ease;
  
  &:hover {
    transform: translateX(4px);
  }
}

.logo-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.logo-text h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 4px;
  letter-spacing: 0.5px;
  background: linear-gradient(45deg, #fff, #e0e7ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.logo-text p {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
  font-weight: 500;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.sidebar-nav {
  flex: 1;
  padding: 24px 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  margin: 0 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    width: 3px;
    background: linear-gradient(to bottom, #667eea, #764ba2);
    transform: scaleY(0);
    transition: transform 0.3s ease;
  }
  
  &:hover {
    background: rgba(255, 255, 255, 0.1);
    
    &::before {
      transform: scaleY(1);
    }
    
    .nav-arrow {
      opacity: 1;
      transform: translateX(0);
    }
  }
  
  &.active {
    background: rgba(255, 255, 255, 0.15);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    
    &::before {
      transform: scaleY(1);
    }
    
    .nav-text {
      font-weight: 600;
    }
    
    .nav-icon {
      transform: scale(1.1);
    }
  }
}

.nav-icon {
  font-size: 20px;
  width: 24px;
  text-align: center;
  transition: transform 0.3s ease;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.nav-text {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 0.3px;
  transition: all 0.3s ease;
}

.nav-arrow {
  font-size: 20px;
  opacity: 0;
  transform: translateX(-8px);
  transition: all 0.3s ease;
  color: rgba(255, 255, 255, 0.7);
}

.sidebar-footer {
  padding: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  
  &:hover {
    background: rgba(255, 255, 255, 0.15);
    transform: translateY(-2px);
  }
}

.admin-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 18px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.admin-details {
  flex: 1;
  min-width: 0;
}

.admin-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.admin-role {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}

/* 主内容区 */
.main-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* 头部样式 */
.header {
  height: auto;
  padding: 20px 32px;
  background: white;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  z-index: 10;
}

.header-left {
  flex: 1;
  min-width: 0;
}

.page-title h3 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 4px;
  letter-spacing: -0.3px;
  line-height: 1.2;
}

.page-title p {
  font-size: 14px;
  color: #666;
  margin: 0;
  line-height: 1.4;
  max-width: 600px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-btn {
  width: 40px;
  height: 40px;
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
    background: rgba(102, 126, 234, 0.1);
    color: #667eea;
    border-color: rgba(102, 126, 234, 0.2);
    transform: translateY(-1px);
  }
  
  svg {
    stroke: currentColor;
  }
  
  &.refresh {
    &:hover {
      animation: spin 0.6s ease;
    }
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.header-back {
  margin: 0 8px;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 10px;
  border: 1px solid rgba(255, 107, 107, 0.3);
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.1), rgba(255, 142, 142, 0.1));
  color: #ff6b6b;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    background: linear-gradient(135deg, rgba(255, 107, 107, 0.2), rgba(255, 142, 142, 0.2));
    color: #ff4d4d;
    border-color: rgba(255, 107, 107, 0.5);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(255, 107, 107, 0.2);
  }
  
  .logout-icon {
    stroke: currentColor;
  }
}

/* 主要内容区域 */
.main-content {
  padding: 0;
  background: transparent;
  overflow: auto;
  position: relative;
}

.content-wrapper {
  padding: 32px;
  min-height: calc(100vh - 120px);
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    width: 80px;
    
    .logo-text,
    .nav-text,
    .nav-arrow,
    .admin-details {
      display: none;
    }
    
    .logo {
      justify-content: center;
    }
    
    .nav-item {
      justify-content: center;
      padding: 16px;
    }
    
    .admin-info {
      justify-content: center;
    }
  }
  
  .header {
    padding: 16px 20px;
  }
  
  .page-title h3 {
    font-size: 20px;
  }
  
  .content-wrapper {
    padding: 20px;
  }
  
  .logout-btn span {
    display: none;
  }
  
  .logout-btn {
    padding: 10px;
    width: 40px;
    height: 40px;
  }
}
</style>