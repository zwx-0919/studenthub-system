import { useUserStore } from "@/store/user";

let ws = null;

export const parseWsMessage = (raw) => {
  if (raw === "pong") return { type: 0, text: "pong", bizId: null };
  try {
    const obj = JSON.parse(raw);
    if (typeof obj === "object" && obj) {
      return {
        type: Number(obj.type || 1),
        text: String(obj.text || ""),
        bizId: obj.bizId ?? null
      };
    }
  } catch (e) {
    // 兼容历史纯文本消息
  }
  return { type: 1, text: String(raw || ""), bizId: null };
};

export const connectWs = (onMessage) => {
  const store = useUserStore();
  if (!store.user?.id) return null;
  if (ws && ws.readyState === WebSocket.OPEN) return ws;

  ws = new WebSocket("ws://localhost:8080/ws/message");
  ws.onopen = () => {
    ws.send(`bind:${store.user.id}`);
    ws.send("ping");
  };
  ws.onmessage = (evt) => {
    if (onMessage) onMessage(parseWsMessage(evt.data));
  };
  ws.onclose = () => {
    ws = null;
  };
  return ws;
};

export const closeWs = () => {
  if (ws) ws.close();
  ws = null;
};
