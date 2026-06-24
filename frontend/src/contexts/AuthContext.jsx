import React, { createContext, useContext, useMemo, useState } from 'react';
import api from '../lib/api';

const AuthContext = createContext(null);

function loginEndpoints(role) {
  if (role === 'admin') return ['/api/auth/admin/login'];
  if (role === 'provider') return ['/api/auth/provider/login'];
  if (role === 'customer') return ['/api/auth/customer/login'];
  return ['/api/auth/customer/login', '/api/auth/provider/login', '/api/auth/admin/login'];
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('servicelink_user');
    return saved ? JSON.parse(saved) : null;
  });

  const login = async (identifier, password, role = 'auto') => {
    let lastError;
    for (const endpoint of loginEndpoints(role)) {
      try {
        const { data } = await api.post(endpoint, { identifier, password });
        localStorage.setItem('servicelink_token', data.token);
        localStorage.setItem('servicelink_user', JSON.stringify(data.user));
        setUser(data.user);
        return data;
      } catch (error) {
        lastError = error;
      }
    }
    throw lastError;
  };

  const register = async (username, email, password, role = 'customer') => {
    const params = new URLSearchParams({
      name: username,
      username,
      email,
      password,
      role,
    });
    const { data } = await api.post(`/api/auth/register?${params.toString()}`);
    localStorage.setItem('servicelink_token', data.token);
    localStorage.setItem('servicelink_user', JSON.stringify(data.user));
    setUser(data.user);
    return data;
  };

  const logout = () => {
    localStorage.removeItem('servicelink_token');
    localStorage.removeItem('servicelink_user');
    setUser(null);
  };

  const value = useMemo(() => ({ user, login, register, logout }), [user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}

