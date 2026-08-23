import { useState } from 'react'
import { ArrowRight, Eye, EyeOff, LockKeyhole, Mail } from 'lucide-react'
import { useLocation, useNavigate } from 'react-router-dom'
import AuthLayout from '../components/AuthLayout'
import { useAuth } from '../context/AuthContext'
import api from '../api/axios'

function errorMessage(error) {
  return error.response?.data?.message || error.response?.data?.error || 'Those credentials did not work. Please try again.'
}

export default function Login() {
  const [form, setForm] = useState({ identifier: '', password: '' })
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  function update(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function submit(event) {
    event.preventDefault()
    setError('')
    setIsLoading(true)
    try {
      const { data } = await api.post('/auth/login', form)
      login(data.token, data)
      navigate(location.state?.from?.pathname || '/dashboard', { replace: true })
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <AuthLayout eyebrow="Your people, in one place" title="Make room for better connections." description="A calmer way to keep every important relationship close, considered, and easy to find." footerText="New to Kinfolk?" footer="Create an account" footerLink="/register">
      <div className="form-heading"><p className="eyebrow">Welcome back</p><h2>Sign in to your address book</h2><p>Enter your details to continue.</p></div>
      {location.state?.registered && <div className="form-success" role="status">Account created. You can sign in now.</div>}
      {error && <div className="form-alert" role="alert">{error}</div>}
      <form onSubmit={submit} className="auth-form">
        <label htmlFor="identifier">Email or phone<input id="identifier" name="identifier" value={form.identifier} onChange={update} required autoComplete="username" placeholder="you@example.com" /><Mail size={18} /></label>
        <label htmlFor="password">Password<div className="password-input"><input id="password" name="password" type={showPassword ? 'text' : 'password'} value={form.password} onChange={update} required autoComplete="current-password" placeholder="Enter your password" /><button type="button" className="icon-button" onClick={() => setShowPassword(!showPassword)} aria-label={showPassword ? 'Hide password' : 'Show password'}>{showPassword ? <EyeOff size={18} /> : <Eye size={18} />}</button></div><LockKeyhole size={18} /></label>
        <button className="submit-button" disabled={isLoading}>{isLoading ? 'Signing in...' : 'Sign in'} {!isLoading && <ArrowRight size={18} />}</button>
      </form>
    </AuthLayout>
  )
}
