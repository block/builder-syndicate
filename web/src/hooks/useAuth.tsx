import { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react'
import { WhoamiResponse } from '../types/api'
import { fetchWhoami, login as apiLogin, logout as apiLogout } from '../api/auth'

interface AuthContextValue {
  user: WhoamiResponse | null
  isLoggedIn: boolean
  isLoading: boolean
  login: (username: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<WhoamiResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    fetchWhoami()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setIsLoading(false))
  }, [])

  const login = useCallback(async (username: string) => {
    await apiLogin(username)
    const whoami = await fetchWhoami()
    setUser(whoami)
  }, [])

  const logout = useCallback(async () => {
    await apiLogout()
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, isLoggedIn: user !== null, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
