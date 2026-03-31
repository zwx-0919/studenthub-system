import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "@/store/user";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", component: () => import("@/views/Login.vue") },
  { path: "/bind-email", component: () => import("@/views/BindEmail.vue") },
  {
    path: "/student",
    component: () => import("@/views/student/StudentLayout.vue"),
    meta: { role: 1 },
    children: [
      { path: "home", component: () => import("@/views/student/Home.vue") },
      { path: "forum", component: () => import("@/views/student/ForumList.vue") },
      { path: "check", component: () => import("@/views/student/Check.vue") },
      { path: "leave", component: () => import("@/views/student/Leave.vue") },
      { path: "course-exam", component: () => import("@/views/student/CourseExam.vue") },
      { path: "ai", component: () => import("@/views/student/AIConsult.vue") },
      { path: "message", component: () => import("@/views/student/MessageCenter.vue") },
      { path: "profile", component: () => import("@/views/Profile.vue") },
      { path: "notice/:id", component: () => import("@/views/student/NoticeDetail.vue") },
      { path: "post/:id", component: () => import("@/views/student/PostDetail.vue") },
      { path: "user/:id", component: () => import("@/views/student/UserHome.vue") },
      { path: "chat/:id", component: () => import("@/views/student/PrivateChat.vue") }
    ]
  },
  {
    path: "/counselor",
    component: () => import("@/views/counselor/CounselorLayout.vue"),
    meta: { role: 2 },
    children: [
      { path: "leave", component: () => import("@/views/counselor/LeaveApprove.vue") },
      { path: "check", component: () => import("@/views/counselor/CheckStats.vue") },
      { path: "profile", component: () => import("@/views/Profile.vue") }
    ]
  },
  {
    path: "/admin",
    component: () => import("@/views/admin/AdminLayout.vue"),
    meta: { role: 3 },
    children: [
      { path: "users", component: () => import("@/views/admin/UserManage.vue") },
      { path: "notice", component: () => import("@/views/admin/NoticeManage.vue") },
      { path: "courses", component: () => import("@/views/admin/CourseManage.vue") },
      { path: "exams", component: () => import("@/views/admin/ExamManage.vue") },
      { path: "posts", component: () => import("@/views/admin/PostManage.vue") }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const store = useUserStore();
  if (!store.token) {
    store.restoreTokenByUser();
  }
  if (to.path === "/login") return true;
  if (!store.token) return "/login";
  const allowWithoutEmail = ["/student/profile", "/counselor/profile", "/admin/users"];
  if (!store.user?.email && to.path !== "/bind-email" && !allowWithoutEmail.includes(to.path)) {
    return "/bind-email";
  }
  if (store.user?.email && to.path === "/bind-email") {
    return store.defaultRoute();
  }
  if (to.meta?.role && store.role !== to.meta.role) {
    return store.defaultRoute();
  }
  return true;
});

export default router;
