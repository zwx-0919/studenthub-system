import { defineStore } from "pinia";
import request from "@/utils/request";

export const useUserStore = defineStore(
  "user",
  {
    state: () => ({
      token: "",
      user: null,
      role: null,
      unreadCount: 0
    }),
    actions: {
      async login(payload) {
        const res = await request.post("/user/login", payload);
        this.user = res.data.user;
        this.role = res.data.user?.role;
        const uid = this.user?.id;
        const token = res.data.token;
        if (uid && token) {
          localStorage.setItem(`user_${uid}_token`, token);
          localStorage.setItem("active_user_id", String(uid));
          this.token = token;
        }
      },
      restoreTokenByUser() {
        const uid = this.user?.id || Number(localStorage.getItem("active_user_id"));
        if (!uid) return;
        const t = localStorage.getItem(`user_${uid}_token`) || "";
        this.token = t;
      },
      defaultRoute() {
        if (this.role === 1) return "/student/home";
        if (this.role === 2) return "/counselor/leave";
        if (this.role === 3) return "/admin/users";
        return "/login";
      },
      setEmail(email) {
        if (this.user) {
          this.user.email = email;
        }
      },
      async loadUnreadCount() {
        try {
          const res = await request.get("/api/message/unread-count");
          this.unreadCount = res.data.unreadCount || 0;
        } catch (_) {
          this.unreadCount = 0;
        }
      },
      logout() {
        const uid = this.user?.id;
        if (uid) {
          localStorage.removeItem(`user_${uid}_token`);
          localStorage.removeItem("active_user_id");
        }
        this.$reset();
      }
    },
    persist: true
  }
);
