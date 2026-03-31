import axios from "axios";
import { useUserStore } from "@/store/user";
import router from "@/router";

const request = axios.create({
  baseURL: "",
  timeout: 12000
});

request.interceptors.request.use((config) => {
  const store = useUserStore();
  const uid = store.user?.id || localStorage.getItem("active_user_id");
  const tokenByUid = uid ? localStorage.getItem(`user_${uid}_token`) : "";
  const token = tokenByUid || store.token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

request.interceptors.response.use(
  (res) => {
    const data = res.data;
    if (typeof data?.code !== "undefined") {
      if (data.code === 1) return data;
      return Promise.reject(new Error(data.msg || "请求失败"));
    }
    return data;
  },
  (err) => {
    if (err?.response?.status === 401) {
      const store = useUserStore();
      store.logout();
      router.replace("/login");
    }
    return Promise.reject(err);
  }
);

export default request;
