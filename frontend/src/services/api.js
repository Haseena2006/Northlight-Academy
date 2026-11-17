import axios from 'axios';

const BASE = import.meta.env.VITE_API_BASE_URL || '/api';
// Custom header required by the backend on cookie-authenticated endpoints (CSRF defence in depth).
export const CSRF = { 'X-Requested-With': 'NorthlightWeb' };

let accessToken = null; // kept in memory only, never in localStorage
let onAuthFailure = () => {};
export const setAccessToken = (t) => { accessToken = t; };
export const setAuthFailureHandler = (fn) => { onAuthFailure = fn; };

const api = axios.create({ baseURL: BASE, withCredentials: true });

api.interceptors.request.use((config) => {
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`;
  return config;
});

let refreshing = null; // shared promise so concurrent 401s trigger a single rotation
export function refreshSession() {
  if (!refreshing) {
    refreshing = axios
      .post(`${BASE}/auth/refresh`, null, { withCredentials: true, headers: CSRF })
      .then((r) => { accessToken = r.data.accessToken; return r.data; })
      .finally(() => { refreshing = null; });
  }
  return refreshing;
}

const NO_RETRY = ['/auth/login', '/auth/register', '/auth/refresh', '/auth/logout'];
api.interceptors.response.use(
  (r) => r,
  async (err) => {
    const { config, response } = err;
    if (response?.status === 401 && config && !config._retry && !NO_RETRY.some((p) => config.url?.startsWith(p))) {
      config._retry = true;
      try { await refreshSession(); return api(config); }
      catch { accessToken = null; onAuthFailure(); }
    }
    return Promise.reject(err);
  }
);

export function errMsg(e) {
  const d = e?.response?.data;
  if (d?.errors) return Object.values(d.errors)[0];
  if (d?.message) return d.message;
  if (e?.code === 'ERR_NETWORK') return 'Cannot reach the server. Please check your connection.';
  return 'Something went wrong. Please try again.';
}

export async function downloadFile(url, filename) {
  const r = await api.get(url, { responseType: 'blob' });
  const href = URL.createObjectURL(r.data);
  const a = document.createElement('a'); a.href = href; a.download = filename; a.click();
  URL.revokeObjectURL(href);
}
export default api;
