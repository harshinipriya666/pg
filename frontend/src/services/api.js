import axios from "axios";

const API = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
    "bypass-tunnel-reminder": "true",
  },
});

// Request interceptor to automatically attach authenticated user context
API.interceptors.request.use((config) => {
  const userStr = localStorage.getItem("user");
  if (userStr) {
    try {
      const user = JSON.parse(userStr);
      if (user.email) config.headers["X-User-Email"] = user.email;
      if (user.role) config.headers["X-User-Role"] = user.role;
      if (user.userId) config.headers["X-User-Id"] = user.userId;
    } catch (e) {
      // Ignore parse error
    }
  }
  return config;
});

// Dedicated AI API helpers
export const aiAPI = {
  chat: (data) => API.post("/ai/chat", data),
  getRoomRecommendations: (data) => API.post("/ai/room-recommendations", data),
  searchRooms: (query) => API.post("/ai/room-search", { query }),
  assistComplaint: (data) => API.post("/ai/complaint-assistance", data),
  getAdminInsights: () => API.get("/ai/admin-insights"),
  getStatus: () => API.get("/ai/status"),
};

export default API;