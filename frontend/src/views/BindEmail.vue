<template>
  <div class="bind-page">
    <!-- 轮播背景（和登录页同款） -->
    <div class="bg-carousel">
      <div class="bg-item bg1"></div>
      <div class="bg-item bg2"></div>
      <div class="bg-item bg3"></div>
    </div>

    <!-- 高级毛玻璃绑定卡片 -->
    <div class="bind-card">
      <h2>绑定邮箱</h2>
      <p class="subtitle">为了找回密码，请先绑定邮箱（验证码5分钟有效）</p>

      <!-- 邮箱输入 -->
      <div class="form-item">
        <div class="input-box">
          <svg class="icon" viewBox="0 0 24 24" fill="none">
            <path d="M4 4H20C21.1 4 22 4.9 22 6V18C22 19.1 21.1 20 20 20H4C2.9 20 2 19.1 2 18V6C2 4.9 2.9 4 4 4Z" stroke="#666" stroke-width="2"/>
            <path d="M22 6L12 13L2 6" stroke="#666" stroke-width="2"/>
          </svg>
          <input v-model.trim="form.email" placeholder="请输入邮箱" />
        </div>
      </div>

      <!-- 验证码行 -->
      <div class="code-row">
        <div class="input-box">
          <svg class="icon" viewBox="0 0 24 24" fill="none">
            <path d="M8 10V12M12 10V12M16 10V12" stroke="#666" stroke-width="2"/>
            <path d="M3 10V20C3 21.1 3.9 22 5 22H19C20.1 22 21 21.1 21 20V10" stroke="#666" stroke-width="2"/>
            <path d="M21 4H3C1.9 4 1 4.9 1 6V8H23V6C23 4.9 22.1 4 21 4Z" stroke="#666" stroke-width="2"/>
          </svg>
          <input v-model.trim="form.code" placeholder="请输入验证码" />
        </div>
        <button class="code-btn" :disabled="countdown > 0" @click="sendCode">
          {{ countdown > 0 ? `${countdown}s后重发` : "发送验证码" }}
        </button>
      </div>

      <button class="submit-btn" @click="submitBind">确认绑定</button>

      <p class="msg success" v-if="okMsg">{{ okMsg }}</p>
      <p class="msg error" v-if="errMsg">{{ errMsg }}</p>
    </div>
  </div>
</template>

<script setup>
import { onUnmounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import request from "@/utils/request";
import { useUserStore } from "@/store/user";

const router = useRouter();
const store = useUserStore();
const form = reactive({
  email: "",
  code: ""
});
const countdown = ref(0);
const errMsg = ref("");
const okMsg = ref("");
let timer = null;

const isValidEmail = (email) =>
  /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/.test(email);

const startCountDown = () => {
  countdown.value = 60;
  if (timer) clearInterval(timer);
  timer = setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0) {
      clearInterval(timer);
      timer = null;
    }
  }, 1000);
};

const sendCode = async () => {
  errMsg.value = "";
  okMsg.value = "";
  if (!isValidEmail(form.email)) {
    errMsg.value = "请输入正确邮箱格式";
    return;
  }
  try {
    await request.post("/user/send-bind-email-code", null, {
      params: { email: form.email }
    });
    okMsg.value = "验证码已发送，请注意查收";
    startCountDown();
  } catch (e) {
    errMsg.value = e?.message || "发送验证码失败";
  }
};

const submitBind = async () => {
  errMsg.value = "";
  okMsg.value = "";
  if (!isValidEmail(form.email)) {
    errMsg.value = "请输入正确邮箱格式";
    return;
  }
  if (!form.code) {
    errMsg.value = "请输入验证码";
    return;
  }
  try {
    await request.post("/user/bind-email", {
      email: form.email,
      code: form.code
    });
    store.setEmail(form.email);
    okMsg.value = "绑定成功，正在跳转...";
    setTimeout(() => router.replace(store.defaultRoute()), 600);
  } catch (e) {
    errMsg.value = e?.message || "绑定失败";
  }
};

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<style scoped lang="less">
/* 页面整体（和登录页统一） */
.bind-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* 轮播背景（和登录页统一） */
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

/* 毛玻璃绑定卡片（统一风格） */
.bind-card {
  position: relative;
  z-index: 10;
  width: 420px;
  padding: 48px 40px;
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

/* 标题样式 */
h2 {
  text-align: center;
  font-size: 28px;
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

/* 表单样式 */
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

/* 验证码行 */
.code-row {
  display: flex;
  gap: 12px;
  align-items: center;
}
.code-row .input-box {
  flex: 1;
}

/* 按钮样式 */
.code-btn {
  height: 50px;
  width: 130px;
  border-radius: 14px;
  border: none;
  background: linear-gradient(90deg, #409eff, #3b8aff);
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}
.code-btn:disabled {
  background: #aaa;
  cursor: not-allowed;
  transform: none;
}
.code-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.submit-btn {
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
.submit-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

/* 提示信息 */
.msg {
  font-size: 14px;
  text-align: center;
  margin: 0;
}
.success {
  color: #00c48c;
}
.error {
  color: #ff4d4f;
}

/* 响应式 */
@media (max-width: 450px) {
  .bind-card {
    width: 90vw;
    padding: 40px 28px;
  }
}
</style>