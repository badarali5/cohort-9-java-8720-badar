import { LogOut, Plus, Search, Users } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const demoContacts = [
  { id: 1, firstName: 'Ada', lastName: 'Lovelace', email: 'ada@example.com', phone: '+1 555 0101' },
  { id: 2, firstName: 'Grace', lastName: 'Hopper', email: 'grace@example.com', phone: '+1 555 0102' },
]

export default function Dashboard() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const name = user?.firstName || 'there'

  function signOut() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <main className="dashboard">
      <header className="dashboard-header">
        <a className="brand" href="/dashboard">
          <Users size={22} /> Kinfolk
        </a>
        <div className="header-actions">
          <span className="user-chip">{user?.email || 'Your account'}</span>
          <button className="text-button" onClick={signOut} type="button">
            <LogOut size={17} /> Sign out
          </button>
        </div>
      </header>

      <section className="dashboard-content">
        <div className="dashboard-intro">
          <div>
            <p className="eyebrow">Your address book</p>
            <h1>Good morning, {name}.</h1>
            <p>Keep the people who matter within reach.</p>
          </div>
          <button className="primary-action" type="button">
            <Plus size={18} /> Add contact
          </button>
        </div>

        <div className="dashboard-toolbar">
          <div className="search-field">
            <Search size={18} />
            <input placeholder="Search contacts" aria-label="Search contacts" />
          </div>
          <span className="contact-count">{demoContacts.length} contacts</span>
        </div>

        <div className="contact-list" aria-label="Contact list">
          {demoContacts.map((contact) => (
            <article className="contact-card" key={contact.id}>
              <div className="contact-avatar">{contact.firstName[0]}{contact.lastName[0]}</div>
              <div className="contact-info">
                <h3>{contact.firstName} {contact.lastName}</h3>
                <p>{contact.email}</p>
                <small>{contact.phone}</small>
              </div>
            </article>
          ))}
        </div>
      </section>
    </main>
  )
}
