import { createContext, useContext, useState } from 'react'
import api, { setAuthToken } from '../api/axios'

const AuthContext = createContext(null)

function readStoredUser() {
  try {
    return JSON.parse(localStorage.getItem('auth_user'))
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(null)
  const [user, setUser] = useState(readStoredUser)

  function login(nextToken, userData) {
    try {
      localStorage.setItem('auth_user', JSON.stringify(userData ?? null))
    } catch {
      // ignore storage quota/private mode issues and keep in-memory state available
    }
    setAuthToken(nextToken)
    setToken(nextToken)
    setUser(userData ?? null)
  }

  function logout() {
    try {
      localStorage.removeItem('auth_user')
    } catch {
      // ignore storage quota/private mode issues and still clear session state in memory
    }
    setAuthToken(null)
    setToken(null)
    setUser(null)
  }

  async function register(data) {
    return api.post('/auth/register', data)
  }

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated: Boolean(token), login, logout, register }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}
