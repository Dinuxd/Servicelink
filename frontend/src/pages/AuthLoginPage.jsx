import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useAuth } from '../contexts/AuthContext';
import toast from 'react-hot-toast';
import { useNavigate, useLocation } from 'react-router-dom';

const schema = z.object({
  identifier: z.string().min(2, 'Enter your username or email'),
  password: z.string().min(6)
});

export default function AuthLoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [showPassword, setShowPassword] = useState(false);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({ resolver: zodResolver(schema) });

  const pathname = location.pathname || '';
  let role = 'customer';
  if (pathname.startsWith('/provider')) role = 'provider';
  else if (pathname.startsWith('/admin')) role = 'admin';

  const config = {
    customer: {
      title: 'Welcome back',
      subtitle: 'Sign in to view your bookings and manage your services.',
      success: 'Signed in as customer',
      redirect: '/bookings',
    },
    provider: {
      title: 'Provider sign in',
      subtitle: 'Access your dashboard, listings and availability tools.',
      success: 'Signed in as provider',
      redirect: '/dashboard',
    },
    admin: {
      title: 'Admin login',
      subtitle: 'Manage users, categories and platform controls.',
      success: 'Signed in as admin',
      redirect: '/admin/users',
    },
  }[role];

  const onSubmit = async (values) => {
    try {
      await login(values.identifier, values.password, role);
      toast.success(config.success);
      navigate(config.redirect, { replace: true });
    } catch (e) {
      toast.error('Invalid credentials');
    }
  };

  return (
    <div className="sl-page-card" style={{ maxWidth: 420, margin: '0 auto' }}>
      <h1 className="sl-page-title">{config.title}</h1>
      <div className="sl-page-subtitle">{config.subtitle}</div>
      <form onSubmit={handleSubmit(onSubmit)} className="sl-stack-v" style={{ marginTop: 18 }}>
        <div>
          <input
            className="sl-input"
            placeholder="Username or email"
            {...register('identifier')}
          />
          {errors.identifier && (
            <div className="text-red-400 text-sm" style={{ marginTop: 4 }}>
              {errors.identifier.message}
            </div>
          )}
        </div>
        <div>
          <div style={{ position: 'relative' }}>
            <input
              type={showPassword ? 'text' : 'password'}
              className="sl-input"
              placeholder="Password"
              {...register('password')}
            />
            <button
              type="button"
              onClick={() => setShowPassword((v) => !v)}
              style={{
                position: 'absolute',
                right: 8,
                top: '50%',
                transform: 'translateY(-50%)',
                fontSize: '0.75rem',
                background: 'transparent',
                border: 'none',
                color: 'var(--sl-text-soft)',
                cursor: 'pointer',
              }}
            >
              {showPassword ? 'Hide' : 'Show'}
            </button>
          </div>
          {errors.password && (
            <div className="text-red-400 text-sm" style={{ marginTop: 4 }}>
              {errors.password.message}
            </div>
          )}
        </div>
        <button
          className="sl-btn sl-btn-primary"
          type="submit"
          disabled={isSubmitting}
          style={{ justifySelf: 'flex-start', marginTop: 4 }}
        >
          {isSubmitting ? 'Signing in…' : 'Sign in'}
        </button>
      </form>
    </div>
  );
}
