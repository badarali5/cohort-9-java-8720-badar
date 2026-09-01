import { useEffect, useMemo, useState } from 'react'
import { LogOut, Pencil, Plus, Search, Trash2, UserRound, Users } from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import ContactModal from '../components/ContactModal'
import { useAuth } from '../context/AuthContext'

const initialContacts = [
  { id: 1, firstName: 'Ada', lastName: 'Lovelace', title: 'Mathematician', emails: [{ id: 1, value: 'ada@example.com', label: 'Work' }], phones: [{ id: 1, value: '+1 555 0101', label: 'Mobile' }] },
  { id: 2, firstName: 'Grace', lastName: 'Hopper', title: 'Engineer', emails: [{ id: 2, value: 'grace@example.com', label: 'Work' }], phones: [{ id: 2, value: '+1 555 0102', label: 'Mobile' }] },
]

function buildContactSummary(contact) {
  const email = contact.emails?.find((entry) => entry.value)?.value || 'No email'
  const phone = contact.phones?.find((entry) => entry.value)?.value || 'No phone'
  return { email, phone }
}

export default function Dashboard() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [contacts, setContacts] = useState([])
  const [searchTerm, setSearchTerm] = useState('')
  const [modalState, setModalState] = useState(null)
  const [contactToDelete, setContactToDelete] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    async function fetchContacts() {
      try {
        setLoading(true)
        setError('')
        const response = await api.get('/contacts')
        setContacts(response.data?.content ?? response.data ?? [])
      } catch (err) {
        setError('Unable to load contacts right now.')
      } finally {
        setLoading(false)
      }
    }

    fetchContacts()
  }, [])

  const name = user?.firstName || 'there'

  const filteredContacts = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()
    if (!term) return contacts

    return contacts.filter((contact) => {
      const fullName = `${contact.firstName} ${contact.lastName}`.toLowerCase()
      return fullName.includes(term) || (contact.title || '').toLowerCase().includes(term)
    })
  }, [contacts, searchTerm])

  function signOut() {
    logout()
    navigate('/login', { replace: true })
  }

  function openCreateContact() {
    setModalState({ type: 'create', contact: null })
  }

  function openEditContact(contact) {
    setModalState({ type: 'edit', contact })
  }

  function closeModal() {
    setModalState(null)
  }

  function saveContact(formData) {
    if (!formData.firstName?.trim() || !formData.lastName?.trim()) return

    if (modalState?.type === 'edit' && modalState.contact) {
      setContacts((current) => current.map((contact) => contact.id === modalState.contact.id
        ? { ...contact, ...formData, id: contact.id }
        : contact))
    } else {
      setContacts((current) => [{
        id: Date.now(),
        ...formData,
      }, ...current])
    }

    closeModal()
  }

  function deleteContact() {
    if (!contactToDelete) return
    setContacts((current) => current.filter((contact) => contact.id !== contactToDelete.id))
    setContactToDelete(null)
  }

  return (
    <>
      <main className="dashboard">
        <header className="dashboard-header">
          <a className="brand" href="/dashboard">
            <Users size={22} /> Kinfolk
          </a>
          <div className="header-actions">
            <Link className="text-button" to="/profile">
              <UserRound size={17} /> Profile
            </Link>
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
            <button className="primary-action" type="button" onClick={openCreateContact}>
              <Plus size={18} /> Add contact
            </button>
          </div>

          <div className="dashboard-toolbar">
            <div className="search-field">
              <Search size={18} />
              <input
                placeholder="Search contacts"
                aria-label="Search contacts"
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.target.value)}
              />
            </div>
            <span className="contact-count">{filteredContacts.length} contacts</span>
          </div>

          {loading ? (
            <div className="empty-state">
              <div className="empty-icon"><Users size={28} /></div>
              <h2>Loading contacts…</h2>
              <p>Fetching your address book.</p>
            </div>
          ) : error ? (
            <div className="empty-state">
              <div className="empty-icon"><Users size={28} /></div>
              <h2>Something went wrong</h2>
              <p>{error}</p>
            </div>
          ) : filteredContacts.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon"><Users size={28} /></div>
              <h2>No matching contacts</h2>
              <p>Try a different search, or add your first contact.</p>
              <button className="primary-action" type="button" onClick={openCreateContact}>
                <Plus size={18} /> Add contact
              </button>
            </div>
          ) : (
            <div className="contact-list" aria-label="Contact list">
              {filteredContacts.map((contact) => {
                const summary = buildContactSummary(contact)
                const initials = `${contact.firstName?.[0] || ''}${contact.lastName?.[0] || ''}`.toUpperCase()

                return (
                  <article className="contact-card" key={contact.id}>
                    <div className="contact-avatar">{initials || 'N'}</div>
                    <div className="contact-info">
                      <h3>{contact.firstName} {contact.lastName}</h3>
                      <p>{contact.title || 'No title'}</p>
                      <small>{summary.email}</small>
                      <small>{summary.phone}</small>
                    </div>
                    <div className="contact-actions">
                      <button type="button" className="icon-action" onClick={() => openEditContact(contact)} aria-label={`Edit ${contact.firstName}`}>
                        <Pencil size={16} />
                      </button>
                      <button type="button" className="icon-action danger" onClick={() => setContactToDelete(contact)} aria-label={`Delete ${contact.firstName}`}>
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </article>
                )
              })}
            </div>
          )}
        </section>
      </main>

      {modalState && (
        <ContactModal
          contact={modalState.type === 'edit' ? modalState.contact : null}
          onClose={closeModal}
          onSave={saveContact}
        />
      )}

      {contactToDelete && (
        <div className="modal-backdrop" role="dialog" aria-modal="true">
          <div className="modal-card confirm-card">
            <h2>Delete contact?</h2>
            <p>
              Are you sure you want to remove {contactToDelete.firstName} {contactToDelete.lastName} from your address book?
            </p>
            <div className="modal-actions">
              <button type="button" className="secondary-button" onClick={() => setContactToDelete(null)}>Cancel</button>
              <button type="button" className="danger-button" onClick={deleteContact}>Delete</button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
