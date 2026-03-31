<template>
  <button v-if="showBack" class="back-btn" @click="goBack">← 返回</button>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

const hiddenPaths = new Set(["/", "/login", "/bind-email", "/student/home"]);
const showBack = computed(() => !hiddenPaths.has(route.path));

const goBack = () => router.go(-1);
</script>

<style scoped lang="less">
.back-btn {
  border: none;
  background: transparent;
  color: #4f46e5;
  font-size: 15px;
  cursor: pointer;
  padding: 6px 8px;
  border-radius: 8px;
}
.back-btn:hover {
  background: #f1edff;
}
</style>
