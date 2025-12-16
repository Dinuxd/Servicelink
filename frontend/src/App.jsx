import React from 'react'
import { Routes, Route, Link, NavLink, Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './contexts/AuthContext'
import { useQuery } from '@tanstack/react-query'
import ProtectedRoute from './components/ProtectedRoute'
import ListingsPage from './pages/ListingsPage.jsx'
import ListingDetailPage from './pages/ListingDetailPage.jsx'
import ProviderDashboard from './pages/ProviderDashboard.jsx'
import AuthLoginPage from './pages/AuthLoginPage.jsx'
import AuthRegisterPage from './pages/AuthRegisterPage.jsx'
import CustomerBookingsPage from './pages/CustomerBookingsPage.jsx'
import BookingDetailPage from './pages/BookingDetailPage.jsx'
import ProviderAvailabilityPage from './pages/ProviderAvailabilityPage.jsx'
import AdminUsersPage from './pages/AdminUsersPage.jsx'
import AdminCategoriesPage from './pages/AdminCategoriesPage.jsx'

function useHeroBookings() {
  return useQuery({
    queryKey: ['heroBookings'],
    queryFn: async () => {
      const res = await api.get('/api/bookings/summary')
      return res.data || []
    },
  })
}

function Layout({ children }) {
  const { user, logout } = useAuth()

  const roles = user?.roleNames || user?.roles || []
  const isUser = roles.includes('ROLE_USER')
  const isProvider = roles.includes('ROLE_PROVIDER')
  const isAdmin = roles.includes('ROLE_ADMIN')

  return (
    <div className="sl-app-shell">
      <header className="sl-app-header">
        <div className="sl-app-header-inner">
          <Link to="/" className="sl-logo">
            <span className="sl-logo-mark">SL</span>
            <span>
              ServiceLink
              <div className="sl-logo-sub">Trusted local pros, on demand.</div>
            </span>
          </Link>

          <nav className="sl-nav-main">
            {/* NOT LOGGED IN */}
            {!user && (
              <>
                <NavLink
                  to="/listings"
                  className={({ isActive }) =>
                    isActive ? 'sl-nav-link sl-nav-link-active' : 'sl-nav-link'
                  }
                >
                  Browse services
                </NavLink>
                <NavLink
                  to="/login"
                  className={({ isActive }) =>
                    isActive ? 'sl-nav-link sl-nav-link-active' : 'sl-nav-link'
                  }
                >
                  Login
                </NavLink>
                <NavLink to="/register" className="sl-nav-link-primary">
                  Sign up
                </NavLink>
              </>
            )}

            {/* ROLE_USER */}
            {user && isUser && !isProvider && !isAdmin && (
              <>
                <NavLink
                  to="/listings"
                  className={({ isActive }) =>
                    isActive ? 'sl-nav-link sl-nav-link-active' : 'sl-nav-link'
                  }
                >
                  Browse services
                </NavLink>
                <NavLink
                  to="/bookings"
                  className={({ isActive }) =>
                    isActive ? 'sl-nav-link sl-nav-link-active' : 'sl-nav-link'
                  }
                >
                  My bookings
                </NavLink>
              </>
            )}

            {/* ROLE_PROVIDER */}
            {user && isProvider && !isAdmin && (
              <>
                <NavLink
                  to="/dashboard"
                  className={({ isActive }) =>
                    isActive ? 'sl-nav-link sl-nav-link-active' : 'sl-nav-link'
                  }
                >
                  Provider dashboard
                </NavLink>
                <NavLink
                  to="/availability"
                  className={({ isActive }) =>
                    isActive ? 'sl-nav-link sl-nav-link-active' : 'sl-nav-link'
                  }
                >
                  Manage availability
                </NavLink>
                <NavLink
                  to="/listings"
                  className={({ isActive }) =>
                    isActive ? 'sl-nav-link sl-nav-link-active' : 'sl-nav-link'
                  }
                >
                  Browse services
                </NavLink>
              </>
            )}

            {/* ROLE_ADMIN */}
            {user && isAdmin && (
              <>
                <NavLink
                  to="/admin/users"
                  className={({ isActive }) =>
                    isActive ? 'sl-nav-link sl-nav-link-active' : 'sl-nav-link'
                  }
                >
                  Users
                </NavLink>
                <NavLink
                  to="/admin/categories"
                  className={({ isActive }) =>
                    isActive ? 'sl-nav-link sl-nav-link-active' : 'sl-nav-link'
                  }
                >
                  Categories
                </NavLink>
              </>
            )}
          </nav>

          {user && (
            <div className="sl-nav-account">
              <div className="sl-nav-account-meta">
                <div className="sl-nav-account-name">{user.name || user.email}</div>
                {roles.length > 0 && (
                  <div className="sl-nav-account-role">{roles[0]}</div>
                )}
              </div>
              <button
                type="button"
                className="sl-btn sl-btn-secondary"
                onClick={logout}
              >
                Logout
              </button>
            </div>
          )}
        </div>
      </header>

      <main className="sl-app-main">
        <div className="sl-main-inner">{children}</div>
      </main>
    </div>
  )
}

function Home() {
  const { user } = useAuth()
  const roles = user?.roleNames || user?.roles || []
  const isUser = roles.includes('ROLE_USER')
  const isProvider = roles.includes('ROLE_PROVIDER')
  const isAdmin = roles.includes('ROLE_ADMIN')

  let primaryCtaTo = '/listings'
  let primaryCtaLabel = 'Explore services'
  let secondaryCtaTo = '/provider/register'
  let secondaryCtaLabel = 'Become a provider'

  if (user) {
    if (isAdmin) {
      primaryCtaTo = '/admin/users'
      primaryCtaLabel = 'Go to admin panel'
      secondaryCtaTo = undefined
    } else if (isProvider) {
      primaryCtaTo = '/dashboard'
      primaryCtaLabel = 'Go to provider dashboard'
      secondaryCtaTo = '/availability'
      secondaryCtaLabel = 'Manage availability'
    } else if (isUser) {
      primaryCtaTo = '/bookings'
      primaryCtaLabel = 'Go to my bookings'
      secondaryCtaTo = '/listings'
      secondaryCtaLabel = 'Browse more services'
    }
  }

  const { data: heroBookings } = useHeroBookings()
  const totalBookings = heroBookings?.length ?? 0
  const topThree = (heroBookings || []).slice(0, 3)

  return (
    <div className="sl-hero-grid">
      <section>
        <div className="sl-hero-kicker">
          <span className="sl-hero-kicker-dot" />
          Book real-world services in minutes
        </div>
        <h1 className="sl-hero-title">
          Where <span>local pros</span> meet ready-to-book customers.
        </h1>
        <p className="sl-hero-body">
          Search across cleaning, repairs, coaching and more. See who&apos;s available,
          lock in a time, and keep every detail in one simple, modern workspace.
        </p>
        <div className="sl-hero-actions">
          <Link to={primaryCtaTo} className="sl-btn sl-btn-primary">
            {primaryCtaLabel}
          </Link>
          {!isAdmin && secondaryCtaTo && (
            <Link to={secondaryCtaTo} className="sl-btn sl-btn-secondary">
              {secondaryCtaLabel}
            </Link>
          )}
        </div>
        <div className="sl-hero-metrics-row">
          <div className="sl-hero-metric">
            <div className="sl-hero-metric-label">Bookings powered by MongoDB</div>
            <div className="sl-hero-metric-value">
              {String(totalBookings || 3).padStart(2, '0')}<span> this week</span>
            </div>
          </div>
          <div className="sl-hero-metric">
            <div className="sl-hero-metric-label">Avg. response time</div>
            <div className="sl-hero-metric-value">&lt; 5 min</div>
          </div>
          <div className="sl-hero-metric">
            <div className="sl-hero-metric-label">Cities live in demo</div>
            <div className="sl-hero-metric-value">Multi-city</div>
          </div>
        </div>
      </section>

      <aside className="sl-hero-panel" aria-label="Live bookings overview">
        <header className="sl-hero-panel-header">
          <div>
            <div className="sl-hero-panel-title">Live booking feed</div>
            <div className="sl-hero-panel-subtitle">
              Pulled directly from your MongoDB-backed Spring Boot API.
            </div>
          </div>
          <div className="sl-hero-panel-avatars" aria-hidden="true">
            <div className="sl-hero-panel-avatar" />
            <div className="sl-hero-panel-avatar" />
            <div className="sl-hero-panel-avatar" />
          </div>
        </header>

        <div className="sl-hero-panel-tabs" aria-hidden="true">
          <button className="sl-hero-panel-tab sl-hero-panel-tab-active" type="button">
            Upcoming
          </button>
          <button className="sl-hero-panel-tab" type="button">
            In progress
          </button>
          <button className="sl-hero-panel-tab" type="button">
            Completed
          </button>
        </div>

        <div className="sl-hero-panel-badge">
          Showing <strong>{topThree.length}</strong> of{' '}
          <strong>{totalBookings}</strong> latest bookings
        </div>

        <div className="sl-hero-panel-list">
          {topThree.map((b) => (
            <div key={b.id} className="sl-hero-panel-row">
              <div className="sl-hero-panel-row-left">
                <div className="sl-pill">
                  <span className="sl-pill-dot" />
                  {b.categoryName || 'Service'} · {b.status}
                </div>
                <div className="sl-hero-panel-row-title">{b.serviceTitle}</div>
                <div className="sl-hero-panel-row-sub">
                  Client: {b.clientName} · Provider: {b.providerName}
                </div>
              </div>
              <div className="sl-hero-panel-row-right">
                <div className="sl-hero-panel-row-time">{b.scheduledLabel}</div>
                <div className="sl-hero-panel-row-tag">Marketplace demo</div>
              </div>
            </div>
          ))}
          {!topThree.length && (
            <div className="sl-empty" style={{ marginTop: 8 }}>
              No recent bookings yet. As bookings are created, they&apos;ll appear here
              instantly.
            </div>
          )}
        </div>

        <footer className="sl-hero-panel-footer">
          <div>
            <span>Built with </span>
            <strong>Spring Boot · MongoDB · React</strong>
          </div>
          <div className="sl-hero-panel-footer-meta">Ideal for demos and internal tools.</div>
        </footer>
      </aside>
    </div>
  )
}

function SimplePage({ title, subtitle, children }) {
  return (
    <div className="sl-page-card">
      <div className="sl-page-title">{title}</div>
      {subtitle && <div className="sl-page-subtitle">{subtitle}</div>}
      {children}
    </div>
  )
}

function AuthRoute({ children }) {
  const { user } = useAuth()
  const location = useLocation()
  if (user) {
    return <Navigate to={location.state?.from || '/'} replace />
  }
  return children
}

export default function App() {
  return (
    <Layout>
      <Routes>
        {/* public routes */}
        <Route path="/" element={<Home />} />
        <Route path="/listings" element={<ListingsPage />} />
        <Route path="/listings/:id" element={<ListingDetailPage />} />

        {/* auth routes (blocked when already logged in) */}
        <Route
          path="/login"
          element={
            <AuthRoute>
              <AuthLoginPage />
            </AuthRoute>
          }
        />
        <Route
          path="/register"
          element={
            <AuthRoute>
              <AuthRegisterPage />
            </AuthRoute>
          }
        />
        <Route
          path="/provider/register"
          element={
            <AuthRoute>
              <AuthRegisterPage />
            </AuthRoute>
          }
        />

        {/* Provider-only area */}
        <Route element={<ProtectedRoute requireRole="ROLE_PROVIDER" />}>
          <Route path="/dashboard" element={<ProviderDashboard />} />
          <Route path="/availability" element={<ProviderAvailabilityPage />} />
        </Route>

        {/* User-only area */}
        <Route element={<ProtectedRoute requireRole="ROLE_USER" />}>
          <Route path="/bookings" element={<CustomerBookingsPage />} />
          <Route path="/bookings/:id" element={<BookingDetailPage />} />
        </Route>

        {/* Admin-only area */}
        <Route element={<ProtectedRoute requireRole="ROLE_ADMIN" />}>
          <Route path="/admin/users" element={<AdminUsersPage />} />
          <Route path="/admin/categories" element={<AdminCategoriesPage />} />
        </Route>
      </Routes>
    </Layout>
  )
}
