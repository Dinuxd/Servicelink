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

  const config = {
    title: 'Sign in',
    subtitle: 'Access your account. We will route you based on your role.',
    success: 'Signed in',
  };

  const onSubmit = async (values) => {
    try {
      const result = await login(values.identifier, values.password, 'auto');
      const roles = result?.user?.roleNames || result?.user?.roles || [];
      let redirect = '/';
      if (roles.includes('ROLE_ADMIN')) redirect = '/admin';
      else if (roles.includes('ROLE_PROVIDER')) redirect = '/dashboard';
      else if (roles.includes('ROLE_USER')) redirect = '/bookings';
      toast.success(config.success);
      navigate(redirect, { replace: true });
    } catch (e) {
      toast.error('Invalid credentials');
    }
  };

  return (
    <div className="sl-auth-shell">
      <video
        className="sl-auth-bg"
        src="/branding/1.mp4"
        autoPlay
        muted
        loop
        playsInline
        aria-hidden
      />
      <div className="sl-auth-overlay" aria-hidden />
      <div className="sl-page-card sl-auth-card">
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
    </div>
  );
}
