import { BrowserRouter, Routes, Route, Link, Navigate, useLocation } from 'react-router-dom'
import { ThemeProvider, useTheme } from './hooks/useTheme'
import { AuthProvider, useAuth } from './hooks/useAuth'
import FeedPage from './pages/FeedPage'
import LoginPage from './pages/LoginPage'
import PostDetailPage from './pages/PostDetailPage'
import CreatePostPage from './pages/CreatePostPage'
import logo from './assets/logo.svg'
import './styles/theme.css'
import './App.css'

function NavBar() {
  const { theme, toggleTheme } = useTheme()
  const { user, isLoggedIn, isLoading, logout } = useAuth()

  return (
    <nav className="nav">
      <Link to="/" className="nav-brand">
        <div className="nav-brand-icon"><img src={logo} alt="BBS" width={24} height={24} /></div>
        <span>Builder Syndicate</span>
      </Link>
      <div className="nav-actions">
        <button
          className="theme-toggle"
          onClick={toggleTheme}
          aria-label={`Switch to ${theme === 'dark' ? 'light' : 'dark'} mode`}
        >
          {theme === 'dark' ? '☀️' : '🌙'}
        </button>
        {!isLoading && (
          isLoggedIn ? (
            <>
              <Link to="/posts/new" className="nav-new-post">+ New Post</Link>
              <span className="nav-user">{user?.displayName}</span>
              <button onClick={logout} className="nav-btn">Logout</button>
            </>
          ) : (
            <Link to="/login" className="nav-link">Login</Link>
          )
        )}
      </div>
    </nav>
  )
}

function RequireAuth({ children }: { children: React.ReactNode }) {
  const { isLoggedIn, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) return <div className="auth-loading">Loading...</div>
  if (!isLoggedIn) return <Navigate to="/login" state={{ from: location.pathname }} replace />
  return <>{children}</>
}

function App() {
  return (
    <BrowserRouter basename="/app">
      <ThemeProvider>
        <AuthProvider>
          <div className="app">
            <NavBar />
            <main className="main">
              <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/" element={<RequireAuth><FeedPage /></RequireAuth>} />
                <Route path="/posts/new" element={<RequireAuth><CreatePostPage /></RequireAuth>} />
                <Route path="/posts/:id" element={<RequireAuth><PostDetailPage /></RequireAuth>} />
              </Routes>
            </main>
          </div>
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  )
}

export default App
