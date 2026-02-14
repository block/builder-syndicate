import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import { ThemeProvider, useTheme } from './hooks/useTheme'
import logo from './assets/logo.svg'
import './styles/theme.css'
import './App.css'

function NavBar() {
  const { theme, toggleTheme } = useTheme()

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
      </div>
    </nav>
  )
}

function App() {
  return (
    <BrowserRouter basename="/app">
      <ThemeProvider>
        <div className="app">
          <NavBar />
          <main className="main">
            <Routes>
              <Route path="/" element={<div className="placeholder"><h1>Builder Syndicate</h1><p>A link aggregator for builders.</p></div>} />
            </Routes>
          </main>
        </div>
      </ThemeProvider>
    </BrowserRouter>
  )
}

export default App
