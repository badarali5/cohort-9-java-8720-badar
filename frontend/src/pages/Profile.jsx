import { useState } from 'react'
import { ArrowLeft, KeyRound, LogOut, UserRound } from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext'

export default function Profile() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [showPasswordForm, setShowPasswordForm] = useState(false)
  const [status, setStatus] = useState({ type: '', message: '' })
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })
  const [isSaving, setIsSaving] = useState(false)

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  async function savePassword(event) {
    event.preventDefault()
    setStatus({ type: '', message: '' })

    if (form.newPassword !== form.confirmPassword) {
      setStatus({ type: 'error', message: 'New passwords do not match.' })
      return
    }

    if (form.newPassword.length < 8) {
      setStatus({ type: 'error', message: 'New password must be at least 8 characters.' })
      return
    }

    setIsSaving(true)

    try {
      await api.put('/users/password', {
        currentPassword: form.currentPassword,
        newPassword: form.newPassword,
      })

      setStatus({ type: 'success', message: 'Password updated successfully.' })
      setShowPasswordForm(false)
      setForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
    } catch (error) {
      const message = error.response?.data?.message || error.response?.data?.error || 'Password update failed.'
      setStatus({ type: 'error', message })
    } finally {
      setIsSaving(false)
    }
  }

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <main className="profile-page">
      <header className="dashboard-header profile-header">
        <Link className="brand" to="/dashboard">
          <ArrowLeft size={20} /> Kinfolk
        </Link>
        <div className="header-actions">
          <button type="button" className="text-button" onClick={handleLogout}>
            <LogOut size={17} /> Sign out
          </button>
        </div>
      </header>

      <section className="profile-content">
        <div className="profile-card">
          <div className="profile-avatar">
            <UserRound size={28} />
          </div>

          <div className="profile-summary">
            <p className="eyebrow">Profile</p>
            <h1>{user?.firstName || 'User'} {user?.lastName || ''}</h1>
            <p>{user?.email || 'No email available'}</p>
          </div>

          <div className="profile-actions">
            <button type="button" className="primary-action" onClick={() => setShowPasswordForm((current) => !current)}>
              <KeyRound size={18} /> Change password
            </button>
          </div>
        </div>

        {showPasswordForm && (
          <form className="password-form" onSubmit={savePassword}>
            <h2>Update your password</h2>
            {status.message && <div className={status.type === 'error' ? 'form-alert' : 'form-success'}>{status.message}</div>}

            <label>
              Current password
              <input type="password" name="currentPassword" value={form.currentPassword} onChange={updateField} required />
            </label>
            <label>
              New password
              <input type="password" name="newPassword" value={form.newPassword} onChange={updateField} required minLength="8" />
            </label>
            <label>
              Confirm new password
              <input type="password" name="confirmPassword" value={form.confirmPassword} onChange={updateField} required minLength="8" />
            </label>

            <div className="modal-actions">
              <button type="button" className="secondary-button" onClick={() => setShowPasswordForm(false)}>Cancel</button>
              <button type="submit" className="primary-action" disabled={isSaving}>
                {isSaving ? 'Saving...' : 'Update password'}
              </button>
            </div>
          </form>
        )}
      </section>
    </main>
  )
}
