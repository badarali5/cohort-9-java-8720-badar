import { LogOut, Plus, Search, Users } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Dashboard() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const name = user?.firstName || 'there'
  function signOut() { logout(); navigate('/login', { replace: true }) }
  return <main className="dashboard"><header className="dashboard-header"><a className="brand" href="/dashboard"><Users size={22} /> Kinfolk</a><div className="header-actions"><span className="user-chip">{user?.email || 'Your account'}</span><button className="text-button" onClick={signOut}><LogOut size={17} /> Sign out</button></div></header><section className="dashboard-content"><div className="dashboard-intro"><div><p className="eyebrow">Your address book</p><h1>Good morning, {name}.</h1><p>Keep the people who matter within reach.</p></div><button className="primary-action"><Plus size={18} /> Add contact</button></div><div className="dashboard-toolbar"><div className="search-field"><Search size={18} /><input placeholder="Search contacts" aria-label="Search contacts" /></div><span className="contact-count">0 contacts</span></div><div className="empty-state"><div className="empty-icon"><Users size={28} /></div><h2>Your circle starts here</h2><p>Add your first contact and make this space yours.</p><button className="primary-action"><Plus size={18} /> Add your first contact</button></div></section></main>
}
