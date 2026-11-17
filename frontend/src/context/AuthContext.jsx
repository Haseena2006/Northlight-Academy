import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import api, { CSRF, refreshSession, setAccessToken, setAuthFailureHandler } from '../services/api.js';

const AuthCtx = createContext(null);
export const useAuth = () => useContext(AuthCtx);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setAuthFailureHandler(() => setUser(null));
    // Restore the session from the HttpOnly refresh cookie (access token lives in memory only).
    refreshSession().then((d) => setUser(d.user)).catch(() => setUser(null)).finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (email, password, rememberMe) => {
    const { data } = await api.post('/auth/login', { email, password, rememberMe });
    setAccessToken(data.accessToken); setUser(data.user); return data.user;
  }, []);
  const register = useCallback((payload) => api.post('/auth/register', payload), []);
  const logout = useCallback(async () => {
    try { await api.post('/auth/logout', null, { headers: CSRF }); } catch { /* ignore */ }
    setAccessToken(null); setUser(null);
  }, []);

  const value = useMemo(() => ({ user, loading, login, register, logout, setUser }), [user, loading, login, register, logout]);
  return <AuthCtx.Provider value={value}>{children}</AuthCtx.Provider>;
}
