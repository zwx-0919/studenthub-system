<template>
  <el-container class="layout-container">
    <el-aside width="260px" class="sider">
      <div class="sider-header">
        <div class="logo-container">
          <div class="logo">EduAdmin</div>
          <span class="subtitle">辅导员工作台</span>
        </div>
      </div>
      <div class="menu-container">
        <el-menu
          router
          :default-active="$route.path"
          background-color="transparent"
          text-color="#a1a5b7"
          active-text-color="#fff"
          class="sidebar-menu"
        >
          <el-menu-item index="/counselor/leave" class="menu-item">
            <el-icon class="menu-icon"><Document /></el-icon>
            <span class="menu-text">请假审批</span>
            <div class="menu-indicator"></div>
          </el-menu-item>
          <el-menu-item index="/counselor/check" class="menu-item">
            <el-icon class="menu-icon"><Calendar /></el-icon>
            <span class="menu-text">打卡统计</span>
            <div class="menu-indicator"></div>
          </el-menu-item>
        </el-menu>
      </div>
      <div class="sider-footer">
        <div class="version">V2.5.0</div>
      </div>
    </el-aside>
    
    <el-container class="main-container">
      <el-header class="header">
        <div class="header-content">
          <div class="header-left">
            <GlobalBackButton />
            <div class="breadcrumb">
              <span class="current-route">工作台</span>
              <el-divider direction="vertical" />
              <span class="page-name">{{ pageName }}</span>
            </div>
          </div>
          
          <div class="header-right">
            <div class="user-menu-wrapper" @mouseenter="showMenu = true" @mouseleave="handleMenuMouseLeave">
              <div class="user-avatar" @click="goProfile">
                <div class="avatar-circle">
                  {{ (store.user?.userName || "辅").slice(0, 1) }}
                </div>
                <div class="user-info">
                  <span class="user-name">{{ store.user?.userName || '辅导员' }}</span>
                  <span class="user-role">管理员</span>
                </div>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </div>
              
              <transition name="fade">
                <div v-if="showMenu" class="user-dropdown" @mouseenter="showMenu = true">
                  <div class="dropdown-header">
                    <div class="dropdown-avatar">
                      {{ (store.user?.userName || "辅").slice(0, 1) }}
                    </div>
                    <div class="dropdown-info">
                      <div class="dropdown-name">{{ store.user?.userName || '辅导员' }}</div>
                      <div class="dropdown-email">{{ store.user?.email || 'counselor@edu.com' }}</div>
                    </div>
                  </div>
                  <div class="dropdown-divider"></div>
                  <div class="dropdown-list">
                    <div class="dropdown-item" @click="goProfile">
                      <el-icon><User /></el-icon>
                      <span>个人信息</span>
                    </div>
                    <div class="dropdown-item" @click="logout">
                      <el-icon><SwitchButton /></el-icon>
                      <span>退出登录</span>
                    </div>
                  </div>
                </div>
              </transition>
            </div>
          </div>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
  
  <MessagePopup
    :visible="popupVisible"
    :text="popupText"
    :unread="popupUnread"
    @detail="viewDetail"
    @close="popupVisible = false"
  />
</template>

<script setup>
import { onMounted, onUnmounted, ref, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { closeWs, connectWs } from "@/utils/ws";
import MessagePopup from "@/components/MessagePopup.vue";
import GlobalBackButton from "@/components/GlobalBackButton.vue";
import { useUserStore } from "@/store/user";
import { Document, Calendar, ArrowDown, User, SwitchButton } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const store = useUserStore();
const popupVisible = ref(false);
const popupUnread = ref(false);
const popupText = ref("");
const showMenu = ref(false);

// 根据路由计算页面名称
const pageName = computed(() => {
  const path = route.path;
  if (path.includes('leave')) return '请假审批';
  if (path.includes('check')) return '打卡统计';
  if (path.includes('profile')) return '个人中心';
  return '工作台';
});

const viewDetail = () => {
  popupUnread.value = false;
  popupVisible.value = false;
  router.push("/counselor/leave");
};

const goProfile = () => {
  showMenu.value = false;
  router.push("/counselor/profile");
};

const logout = () => {
  closeWs();
  store.logout();
  router.replace("/login");
};

// 处理菜单鼠标离开事件
const handleMenuMouseLeave = () => {
  setTimeout(() => {
    if (!document.querySelector('.user-dropdown:hover')) {
      showMenu.value = false;
    }
  }, 100);
};

onMounted(() => {
  connectWs((msg) => {
    if (msg.type === 0) return;
    popupText.value = String(msg.text || "");
    popupUnread.value = true;
    popupVisible.value = true;
  });
});

onUnmounted(() => {
  closeWs();
});
</script>

<style scoped lang="less">
.layout-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  overflow: hidden;
  
  .sider {
    background: linear-gradient(135deg, #1a1f2e 0%, #0f1422 100%);
    box-shadow: 4px 0 20px rgba(0, 0, 0, 0.1);
    display: flex;
    flex-direction: column;
    border-right: none;
    z-index: 10;
    
    .sider-header {
      padding: 24px 0;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      
      .logo-container {
        padding: 0 24px;
        display: flex;
        flex-direction: column;
        gap: 8px;
        
        .logo {
          font-size: 24px;
          font-weight: 800;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
          letter-spacing: 1px;
        }
        
        .subtitle {
          font-size: 12px;
          color: rgba(255, 255, 255, 0.5);
          font-weight: 500;
          letter-spacing: 1px;
        }
      }
    }
    
    .menu-container {
      flex: 1;
      padding: 20px 16px;
      
      .sidebar-menu {
        border: none;
        
        .menu-item {
          height: 48px;
          border-radius: 12px;
          margin-bottom: 8px;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          position: relative;
          overflow: hidden;
          
          .menu-icon {
            font-size: 20px;
            margin-right: 12px;
            transition: all 0.3s ease;
          }
          
          .menu-text {
            font-weight: 600;
            font-size: 14px;
            letter-spacing: 0.5px;
          }
          
          .menu-indicator {
            position: absolute;
            right: 16px;
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            opacity: 0;
            transform: translateX(10px);
            transition: all 0.3s ease;
          }
          
          &:hover {
            background: rgba(255, 255, 255, 0.1) !important;
            transform: translateX(4px);
            
            .menu-icon {
              transform: scale(1.1);
            }
          }
          
          &.is-active {
            background: linear-gradient(135deg, rgba(102, 126, 234, 0.2) 0%, rgba(118, 75, 162, 0.2) 100%) !important;
            border-left: 4px solid #667eea;
            
            .menu-text {
              color: #fff;
              font-weight: 700;
            }
            
            .menu-icon {
              color: #667eea;
              transform: scale(1.1);
            }
            
            .menu-indicator {
              opacity: 1;
              transform: translateX(0);
            }
          }
        }
      }
    }
    
    .sider-footer {
      padding: 20px 24px;
      border-top: 1px solid rgba(255, 255, 255, 0.1);
      
      .version {
        color: rgba(255, 255, 255, 0.3);
        font-size: 12px;
        font-weight: 500;
        letter-spacing: 0.5px;
      }
    }
  }
  
  .main-container {
    overflow: hidden;
    
    .header {
      height: 72px;
      padding: 0 24px;
      background: rgba(255, 255, 255, 0.9);
      backdrop-filter: blur(10px);
      border-bottom: 1px solid rgba(0, 0, 0, 0.05);
      box-shadow: 0 2px 20px rgba(0, 0, 0, 0.05);
      display: flex;
      align-items: center;
      z-index: 9;
      
      .header-content {
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: space-between;
        
        .header-left {
          display: flex;
          align-items: center;
          gap: 20px;
          
          .breadcrumb {
            display: flex;
            align-items: center;
            gap: 12px;
            
            .current-route {
              font-size: 20px;
              font-weight: 700;
              color: #1e293b;
              letter-spacing: -0.5px;
            }
            
            :deep(.el-divider) {
              height: 20px;
              background-color: #cbd5e1;
              margin: 0;
            }
            
            .page-name {
              font-size: 16px;
              color: #64748b;
              font-weight: 500;
            }
          }
        }
        
        .header-right {
          .user-menu-wrapper {
            position: relative;
            
            .user-avatar {
              display: flex;
              align-items: center;
              gap: 12px;
              padding: 8px 16px;
              border-radius: 12px;
              background: #f8fafc;
              border: 1px solid #e2e8f0;
              cursor: pointer;
              transition: all 0.3s ease;
              min-width: 200px;
              
              &:hover {
                background: #f1f5f9;
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
                transform: translateY(-1px);
                
                .dropdown-icon {
                  transform: rotate(180deg);
                }
              }
              
              .avatar-circle {
                width: 40px;
                height: 40px;
                border-radius: 50%;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                display: flex;
                align-items: center;
                justify-content: center;
                font-weight: 700;
                font-size: 16px;
                box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
              }
              
              .user-info {
                display: flex;
                flex-direction: column;
                flex: 1;
                
                .user-name {
                  font-weight: 600;
                  color: #1e293b;
                  font-size: 14px;
                }
                
                .user-role {
                  font-size: 12px;
                  color: #94a3b8;
                  font-weight: 500;
                }
              }
              
              .dropdown-icon {
                color: #94a3b8;
                font-size: 12px;
                transition: transform 0.3s ease;
              }
            }
            
            .user-dropdown {
              position: absolute;
              top: calc(100% + 8px);
              right: 0;
              width: 320px;
              background: white;
              border-radius: 16px;
              box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
              border: 1px solid #e2e8f0;
              z-index: 1000;
              overflow: hidden;
              
              .dropdown-header {
                padding: 20px 24px;
                background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
                display: flex;
                align-items: center;
                gap: 16px;
                border-bottom: 1px solid #e2e8f0;
                
                .dropdown-avatar {
                  width: 48px;
                  height: 48px;
                  border-radius: 50%;
                  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                  color: white;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  font-weight: 700;
                  font-size: 18px;
                  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.3);
                }
                
                .dropdown-info {
                  flex: 1;
                  
                  .dropdown-name {
                    font-weight: 700;
                    color: #1e293b;
                    font-size: 16px;
                    margin-bottom: 4px;
                  }
                  
                  .dropdown-email {
                    font-size: 12px;
                    color: #64748b;
                    font-weight: 500;
                  }
                }
              }
              
              .dropdown-divider {
                height: 1px;
                background: #e2e8f0;
                margin: 0;
              }
              
              .dropdown-list {
                padding: 12px;
                
                .dropdown-item {
                  display: flex;
                  align-items: center;
                  gap: 12px;
                  padding: 12px 16px;
                  border-radius: 12px;
                  cursor: pointer;
                  transition: all 0.3s ease;
                  color: #475569;
                  font-weight: 500;
                  
                  &:hover {
                    background: #f1f5f9;
                    color: #667eea;
                    transform: translateX(4px);
                    
                    .el-icon {
                      color: #667eea;
                    }
                  }
                  
                  .el-icon {
                    font-size: 18px;
                    color: #94a3b8;
                    transition: all 0.3s ease;
                  }
                  
                  span {
                    font-size: 14px;
                  }
                }
              }
            }
          }
        }
      }
    }
    
    .main-content {
      padding: 24px;
      overflow: auto;
      background: transparent;
    }
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 768px) {
  .layout-container {
    .sider {
      width: 80px !important;
      
      .logo-container {
        .logo {
          font-size: 18px;
        }
        
        .subtitle {
          display: none;
        }
      }
      
      .menu-item {
        .menu-text {
          display: none;
        }
        
        .menu-icon {
          margin-right: 0;
        }
      }
    }
    
    .header {
      padding: 0 16px;
      
      .breadcrumb {
        .page-name {
          display: none;
        }
      }
      
      .user-avatar {
        min-width: auto !important;
        padding: 8px !important;
        
        .user-info {
          display: none;
        }
      }
    }
  }
}
</style>