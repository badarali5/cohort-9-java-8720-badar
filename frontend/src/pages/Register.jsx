import { useState } from 'react'
import { ArrowRight, Eye, EyeOff, LockKeyhole, Mail, Phone, UserRound } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import AuthLayout from '../components/AuthLayout'
import { useAuth } from '../context/AuthContext'

function errorMessage(error) {
  const details = error.response?.data
  if (details?.errors && typeof details.errors === 'object') return Object.values(details.errors).join(' ')
  return details?.message || details?.error || 'We could not create your account. Please check your details.'
}

export default function Register() {
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', phone: '', password: '' })
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()

  function update(event) { setForm({ ...form, [event.target.name]: event.target.value }) }

  async function submit(event) {
    event.preventDefault()
    setError('')
    setIsLoading(true)
    try {
      await register(form)
      navigate('/login', { state: { registered: true } })
    } catch (requestError) { setError(errorMessage(requestError)) }
    finally { setIsLoading(false) }
  }

  return (
    <AuthLayout eyebrow="Start with the essentials" title="Good relationships deserve good records." description="Create a private home for the people, details, and moments you do not want to lose." footerText="Already have an account?" footer="Sign in" footerLink="/login">
      <div className="form-heading"><p className="eyebrow">Get started</p><h2>Create your account</h2><p>Your email is required. You can add a phone number for easier sign-in.</p></div>
      {error && <div className="form-alert" role="alert">{error}</div>}
      <form onSubmit={submit} className="auth-form register-form">
        <div className="field-row"><label htmlFor="firstName">First name<input id="firstName" name="firstName" value={form.firstName} onChange={update} required autoComplete="given-name" placeholder="Ada" /><UserRound size={18} /></label><label htmlFor="lastName">Last name<input id="lastName" name="lastName" value={form.lastName} onChange={update} autoComplete="family-name" placeholder="Lovelace" /></label></div>
        <label htmlFor="email">Email<input id="email" name="email" type="email" value={form.email} onChange={update} required autoComplete="email" placeholder="you@example.com" /><Mail size={18} /></label>
        <label htmlFor="phone">Phone <span className="optional">optional</span><input id="phone" name="phone" type="tel" value={form.phone} onChange={update} autoComplete="tel" placeholder="+1 555 0123" /><Phone size={18} /></label>
        <label htmlFor="password">Password<div className="password-input"><input id="password" name="password" type={showPassword ? 'text' : 'password'} value={form.password} onChange={update} required minLength="8" autoComplete="new-password" placeholder="8+ characters" /><button type="button" className="icon-button" onClick={() => setShowPassword(!showPassword)} aria-label={showPassword ? 'Hide password' : 'Show password'}>{showPassword ? <EyeOff size={18} /> : <Eye size={18} />}</button></div><LockKeyhole size={18} /></label>
        <button className="submit-button" disabled={isLoading}>{isLoading ? 'Creating account...' : 'Create account'} {!isLoading && <ArrowRight size={18} />}</button>
      </form>
    </AuthLayout>
  )
}
