import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { fetchDevUsers } from '../api/auth'
import { DevUser } from '../types/api'
import logo from '../assets/logo.svg'
import './LoginPage.css'

function LoginPage() {
  const { isLoggedIn, login } = useAuth()
  const navigate = useNavigate()
  const [devUsers, setDevUsers] = useState<DevUser[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (isLoggedIn) {
      navigate('/', { replace: true })
      return
    }
    fetchDevUsers()
      .then((data) => setDevUsers(data.users))
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load'))
      .finally(() => setIsLoading(false))
  }, [isLoggedIn, navigate])

  const handleLogin = async (username: string) => {
    try {
      setError(null)
      await login(username)
      navigate('/', { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed')
    }
  }

  if (isLoading) return <div className="login-loading">Loading...</div>

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-logo"><img src={logo} alt="BBS" width={48} height={48} /></div>
        <h1 className="login-title">Builder Syndicate</h1>
        <p className="login-subtitle">Knowledge sharing for engineering teams</p>
        <div className="login-divider" />
        <h2 className="login-heading">Dev Login</h2>
        <p className="login-hint">Select a user to sign in:</p>
        {error && <div className="login-error">{error}</div>}
        <div className="login-users">
          {devUsers.map((u) => (
            <button
              key={u.username}
              className="login-user-btn"
              onClick={() => handleLogin(u.username)}
            >
              <span className="login-user-name">{u.displayName}</span>
              <span className="login-user-id">{u.username}</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  )
}

export default LoginPage
