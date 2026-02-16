import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useAuth } from '../contexts/AuthContext';
import toast from 'react-hot-toast';
import { useLocation } from 'react-router-dom';

const schema = z.object({
  username: z.string().min(2, 'Username required'),
  email: z.string().email(),
  password: z.string().min(6)
});

export default function AuthRegisterPage() {
  const { register: registerUser } = useAuth();
  const location = useLocation();
  const isProviderFlow = location.pathname.startsWith('/provider');
  const [showPassword, setShowPassword] = useState(false);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({ resolver: zodResolver(schema) });

  const onSubmit = async (values) => {
    try {
      const role = isProviderFlow ? 'provider' : 'customer';
      await registerUser(values.username, values.email, values.password, role);
      toast.success('Registered. You can now login.');
    } catch (e) {
      toast.error('Registration failed');
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
        <h1 className="sl-page-title">{isProviderFlow ? 'Create your provider account' : 'Create your account'}</h1>
        <div className="sl-page-subtitle">
          {isProviderFlow
            ? 'Publish services, manage your availability and receive bookings.'
            : 'Book services faster or publish your own listings.'}
        </div>
        <form onSubmit={handleSubmit(onSubmit)} className="sl-stack-v" style={{ marginTop: 18 }}>
          <div>
            <input
              className="sl-input"
              placeholder="Username"
              {...register('username')}
            />
            {errors.username && (
              <div className="text-red-400 text-sm" style={{ marginTop: 4 }}>
                {errors.username.message}
              </div>
            )}
          </div>
          <div>
            <input
              className="sl-input"
              placeholder="Email"
              {...register('email')}
            />
            {errors.email && (
              <div className="text-red-400 text-sm" style={{ marginTop: 4 }}>
                {errors.email.message}
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
            {isSubmitting ? 'Registering…' : 'Register'}
          </button>
        </form>
      </div>
    </div>
  );
}
