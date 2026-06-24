import React from 'react';
import { Link, Navigate, Route, Routes } from 'react-router-dom';
import ListingsPage from './pages/ListingsPage';
import AuthLoginPage from './pages/AuthLoginPage';
import AuthRegisterPage from './pages/AuthRegisterPage';
import AdminUsersPage from './pages/AdminUsersPage';
import AdminCategoriesPage from './pages/AdminCategoriesPage';
import { useAuth } from './contexts/AuthContext';

function HomePage() {
  return (
    <div className="sl-page-container">
      <div className="sl-page-card">
        <h1 className="sl-page-title">ServiceLink</h1>
        <p className="sl-page-subtitle">Browse services, register as a customer or provider, and manage admin data.</p>
        <div className="sl-stack-h">
          <Link className="sl-btn sl-btn-primary" to="/listings">Browse services</Link>
          <Link className="sl-btn sl-btn-secondary" to="/login">Sign in</Link>
        </div>
      </div>
    </div>
  );
}

function PlaceholderPage({ title }) {
  return (
    <div className="sl-page-container">
      <div className="sl-page-card">
        <h1 className="sl-page-title">{title}</h1>
        <p className="sl-page-subtitle">This page is ready for the next feature step.</p>
      </div>
    </div>
  );
}

function Layout() {
  const { user, logout } = useAuth();
  return (
    <>
      <nav className="sl-nav">
        <Link to="/">ServiceLink</Link>
        <Link to="/listings">Listings</Link>
        <Link to="/admin/users">Admin Users</Link>
        <Link to="/admin/categories">Admin Categories</Link>
        {user ? (
          <button className="sl-link-button" onClick={logout}>Logout</button>
        ) : (
          <Link to="/login">Login</Link>
        )}
      </nav>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/listings" element={<ListingsPage />} />
        <Route path="/login" element={<AuthLoginPage />} />
        <Route path="/register" element={<AuthRegisterPage />} />
        <Route path="/provider/register" element={<AuthRegisterPage />} />
        <Route path="/admin" element={<Navigate to="/admin/users" replace />} />
        <Route path="/admin/users" element={<AdminUsersPage />} />
        <Route path="/admin/categories" element={<AdminCategoriesPage />} />
        <Route path="/dashboard" element={<PlaceholderPage title="Provider Dashboard" />} />
        <Route path="/bookings" element={<PlaceholderPage title="Bookings" />} />
      </Routes>
    </>
  );
}

export default Layout;

