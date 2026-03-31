<template>
  <div class="login-page">
    <!-- 轮播背景 -->
    <div class="bg-carousel">
      <div class="bg-item bg1"></div>
      <div class="bg-item bg2"></div>
      <div class="bg-item bg3"></div>
    </div>

    <!-- 高级毛玻璃登录卡片 -->
    <div class="login-card">
      <h2>高校学生智能系统</h2>
      <p class="subtitle">智慧校园 · 便捷服务</p>

      <!-- 灵动角色选择卡片（移到顶部） -->
      <div class="role-container">
        <div
          class="role-card"
          :class="{ active: form.role === 1 }"
          @click="form.role = 1"
        >
          👨‍🎓 学生
        </div>
        <div
          class="role-card"
          :class="{ active: form.role === 2 }"
          @click="form.role = 2"
        >
          👨‍🏫 辅导员
        </div>
        <div
          class="role-card"
          :class="{ active: form.role === 3 }"
          @click="form.role = 3"
        >
          🔐 管理员
        </div>
      </div>

      <!-- 账号输入 -->
      <div class="form-item">
        <div class="input-box">
          <svg class="icon" viewBox="0 0 24 24" fill="none">
            <path d="M12 14C13.1046 14 14 13.1046 14 12C14 10.8954 13.1046 10 12 10C10.8954 10 10 10.8954 10 12C10 13.1046 10.8954 14 12 14Z" stroke="#666" stroke-width="2"/>
            <path d="M20 12C20 15.3137 16.4183 18 12 18C7.58172 18 4 15.3137 4 12C4 8.68629 7.58172 6 12 6C16.4183 6 20 8.68629 20 12Z" stroke="#666" stroke-width="2"/>
          </svg>
          <input v-model="form.userAccount" placeholder="请输入账号" />
        </div>
      </div>

      <!-- 密码输入 -->
      <div class="form-item">
        <div class="input-box">
          <svg class="icon" viewBox="0 0 24 24" fill="none">
            <path d="M12 15V17M12 7V9M12 21C16.9706 21 21 16.9706 21 12C21 7.02944 16.9706 3 12 3C7.02944 3 3 7.02944 3 12C3 16.9706 7.02944 21 12 21Z" stroke="#666" stroke-width="2"/>
          </svg>
          <input v-model="form.password" type="password" placeholder="请输入密码" />
        </div>
      </div>

      <button class="login-btn" @click="onLogin">登录系统</button>
      <p class="error-msg" v-if="errMsg">{{ errMsg }}</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store/user";

const router = useRouter();
const store = useUserStore();
const errMsg = ref("");
const form = reactive({
  userAccount: "",
  password: "",
  role: 1,
});

const onLogin = async () => {
  errMsg.value = "";
  try {
    await store.login(form);
    store.restoreTokenByUser();
    await store.loadUnreadCount();

    if (!store.user?.email) {
      window.alert("检测到你尚未绑定邮箱，请先完成绑定（用于忘记密码找回）");
      router.replace("/bind-email");
      return;
    }

    router.push(store.defaultRoute());
  } catch (e) {
    errMsg.value = e?.message || "登录失败";
  }
};
</script>

<style scoped lang="less">
.login-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* 轮播背景 */
.bg-carousel {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}
.bg-item {
  position: absolute;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  animation: carousel 15s infinite;
  opacity: 0;
}
.bg1 { background-image: url("https://picsum.photos/id/1031/1920/1080"); animation-delay: 0s; }
.bg2 { background-image: url("https://picsum.photos/id/1076/1920/1080"); animation-delay: 5s; }
.bg3 { background-image: url("https://picsum.photos/id/143/1920/1080"); animation-delay: 10s; }
@keyframes carousel {
  0% { opacity: 0; }
  10% { opacity: 1; }
  33% { opacity: 1; }
  43% { opacity: 0; }
  100% { opacity: 0; }
}

/* 登录卡片 */
.login-card {
  position: relative;
  z-index: 10;
  width: 400px;
  padding: 45px 35px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(16px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  gap: 24px;
  animation: fadeIn 0.6s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

h2 {
  text-align: center;
  font-size: 26px;
  color: #fff;
  margin: 0;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}
.subtitle {
  text-align: center;
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  margin: -8px 0 0;
}

/* 灵动角色卡片样式 */
.role-container {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}
.role-card {
  flex: 1;
  height: 54px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 500;
  color: #333;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 1, 0.5, 1);
  border: 2px solid transparent;
}
.role-card:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}
.role-card.active {
  background: linear-gradient(135deg, #409eff, #3b8aff);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.5);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
  transform: translateY(-2px);
}

/* 输入框样式 */
.form-item {
  width: 100%;
}
.input-box {
  position: relative;
  width: 100%;
}
.icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  z-index: 1;
}
input {
  width: 100%;
  height: 50px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.9);
  padding: 0 16px 0 48px;
  font-size: 16px;
  box-sizing: border-box;
  transition: all 0.3s;
}
input:focus {
  outline: none;
  border-color: #3b8aff;
  box-shadow: 0 0 0 3px rgba(59, 138, 255, 0.2);
  background: #fff;
}

/* 登录按钮 */
.login-btn {
  height: 52px;
  border-radius: 14px;
  border: none;
  background: linear-gradient(90deg, #409eff, #3b8aff);
  color: #fff;
  font-size: 17px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}
.login-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

/* 错误提示 */
.error-msg {
  color: #ff4d4f;
  font-size: 14px;
  text-align: center;
  margin: 0;
}
</style>