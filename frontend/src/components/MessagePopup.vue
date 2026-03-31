<template>
  <div v-if="visible" class="overlay">
    <div class="popup">
      <div class="title-row">
        <h4>消息提醒</h4>
        <span class="red-dot" v-if="unread"></span>
      </div>
      <p class="content">{{ text }}</p>
      <div class="actions">
        <button class="btn detail" @click="$emit('detail')">查看详情</button>
        <button class="btn close" @click="$emit('close')">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  visible: { type: Boolean, default: false },
  text: { type: String, default: "" },
  unread: { type: Boolean, default: false }
});

defineEmits(["detail", "close"]);
</script>

<style scoped lang="less">
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.28);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.popup {
  width: 420px;
  max-width: calc(100vw - 40px);
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.16);
}
.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
h4 {
  margin: 0;
}
.red-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ff4d4f;
}
.content {
  margin: 14px 0 18px;
  line-height: 1.6;
  color: #333;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.btn {
  border: none;
  border-radius: 8px;
  padding: 8px 14px;
  cursor: pointer;
}
.detail {
  background: #6a55ff;
  color: #fff;
}
.close {
  background: #f2f3f5;
  color: #333;
}
</style>
