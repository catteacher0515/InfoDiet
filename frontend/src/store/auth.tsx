import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { fetchCurrentUser, logout as logoutRequest } from '../api/auth'
import type { LoginUser } from '../types/auth'

interface AuthContextValue {
  user: LoginUser | null
  token: string | null
  ready: boolean
  isAuthenticated: boolean
  setLoginState: (nextUser: LoginUser, token: string) => void
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

const TOKEN_KEY = 'info_diet_token'
const USER_KEY = 'info_diet_user'

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<LoginUser | null>(() => {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? (JSON.parse(raw) as LoginUser) : null
  })
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY))
  const [ready, setReady] = useState(false)

  useEffect(() => {
    async function bootstrap() {
      if (!token) {
        setReady(true)
        return
      }
      try {
        const response = await fetchCurrentUser()
        if (response.code === 0) {
          const nextUser = { ...response.data, token }
          setUser(nextUser)
          localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
        } else {
          clear()
        }
      } catch {
        clear()
      } finally {
        setReady(true)
      }
    }

    void bootstrap()
  }, [token])

  function clear() {
    setUser(null)
    setToken(null)
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  function setLoginState(nextUser: LoginUser, nextToken: string) {
    const next = { ...nextUser, token: nextToken }
    setUser(next)
    setToken(nextToken)
    localStorage.setItem(TOKEN_KEY, nextToken)
    localStorage.setItem(USER_KEY, JSON.stringify(next))
  }

  async function logout() {
    try {
      await logoutRequest()
    } finally {
      clear()
    }
  }

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      token,
      ready,
      isAuthenticated: Boolean(token && user),
      setLoginState,
      logout,
    }),
    [ready, token, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }
  return context
}
