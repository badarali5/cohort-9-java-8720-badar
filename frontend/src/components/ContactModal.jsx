import { useEffect, useState } from 'react'
import { X } from 'lucide-react'

const emptyContact = {
  firstName: '',
  lastName: '',
  title: '',
  emails: [{ id: Date.now(), value: '', label: 'Work' }],
  phones: [{ id: Date.now() + 1, value: '', label: 'Mobile' }],
}

function normalizeContact(value) {
  const safeContact = {
    ...value,
    emails: Array.isArray(value?.emails) && value.emails.length > 0 ? value.emails.map((email) => ({
      id: email.id ?? crypto.randomUUID(),
      value: email.value ?? email.email ?? '',
      label: email.label ?? 'Work',
    })) : [{ id: crypto.randomUUID(), value: '', label: 'Work' }],
    phones: Array.isArray(value?.phones) && value.phones.length > 0 ? value.phones.map((phone) => ({
      id: phone.id ?? crypto.randomUUID(),
      value: phone.value ?? phone.number ?? '',
      label: phone.label ?? 'Mobile',
    })) : [{ id: crypto.randomUUID(), value: '', label: 'Mobile' }],
  }

  return safeContact
}

export default function ContactModal({ contact, onClose, onSave }) {
  const [form, setForm] = useState(normalizeContact(contact ?? emptyContact))
  const [formError, setFormError] = useState('')

  useEffect(() => {
    setForm(normalizeContact(contact ?? emptyContact))
    setFormError('')
  }, [contact])

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  function updateList(listName, index, key, value) {
    setForm((current) => {
      const nextList = [...(current[listName] || [])]
      nextList[index] = { ...nextList[index], [key]: value }
      return { ...current, [listName]: nextList }
    })
  }

  function addListItem(listName, item) {
    setForm((current) => ({
      ...current,
      [listName]: [...(current[listName] || []), item],
    }))
  }

  function removeListItem(listName, index) {
    setForm((current) => ({
      ...current,
      [listName]: (current[listName] || []).filter((_, currentIndex) => currentIndex !== index),
    }))
  }

  function submit(event) {
    event.preventDefault()

    const firstName = form.firstName?.trim() ?? ''
    const lastName = form.lastName?.trim() ?? ''

    if (!firstName || !lastName) {
      setFormError('First name and last name are required.')
      return
    }

    const cleaned = {
      ...form,
      firstName,
      lastName,
      title: form.title.trim(),
      emails: (form.emails || [])
        .filter((email) => email.value?.trim())
        .map((email) => ({ ...email, value: email.value.trim(), label: email.label || 'Work' })),
      phones: (form.phones || [])
        .filter((phone) => phone.value?.trim())
        .map((phone) => ({ ...phone, value: phone.value.trim(), label: phone.label || 'Mobile' })),
    }

    setFormError('')
    onSave(cleaned)
  }

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>{contact ? 'Update contact' : 'Add contact'}</h2>
          <button type="button" className="icon-button ghost" onClick={onClose} aria-label="Close form">
            <X size={18} />
          </button>
        </div>

        <form onSubmit={submit} className="contact-form">
          {formError && <div className="form-alert" role="alert">{formError}</div>}
          <div className="field-grid two-up">
            <label>
              First name
              <input
                value={form.firstName}
                onChange={(event) => {
                  updateField('firstName', event.target.value)
                  if (formError) setFormError('')
                }}
                required
              />
            </label>
            <label>
              Last name
              <input
                value={form.lastName}
                onChange={(event) => {
                  updateField('lastName', event.target.value)
                  if (formError) setFormError('')
                }}
                required
              />
            </label>
          </div>

          <label>
            Title
            <input value={form.title} onChange={(event) => updateField('title', event.target.value)} placeholder="Product designer" />
          </label>

          <div className="inline-section">
            <div className="section-heading">
              <h3>Emails</h3>
              <button type="button" className="ghost-button" onClick={() => addListItem('emails', { id: crypto.randomUUID(), value: '', label: 'Work' })}>Add email</button>
            </div>

            {(form.emails || []).map((email, index) => (
              <div className="field-grid list-item" key={email.id ?? index}>
                <input
                  placeholder="person@example.com"
                  value={email.value}
                  onChange={(event) => updateList('emails', index, 'value', event.target.value)}
                />
                <input
                  placeholder="Work"
                  value={email.label}
                  onChange={(event) => updateList('emails', index, 'label', event.target.value)}
                />
                {(form.emails || []).length > 1 && (
                  <button type="button" className="ghost-button danger" onClick={() => removeListItem('emails', index)}>Remove</button>
                )}
              </div>
            ))}
          </div>

          <div className="inline-section">
            <div className="section-heading">
              <h3>Phones</h3>
              <button type="button" className="ghost-button" onClick={() => addListItem('phones', { id: crypto.randomUUID(), value: '', label: 'Mobile' })}>Add phone</button>
            </div>

            {(form.phones || []).map((phone, index) => (
              <div className="field-grid list-item" key={phone.id ?? index}>
                <input
                  placeholder="+1 555 1234"
                  value={phone.value}
                  onChange={(event) => updateList('phones', index, 'value', event.target.value)}
                />
                <input
                  placeholder="Mobile"
                  value={phone.label}
                  onChange={(event) => updateList('phones', index, 'label', event.target.value)}
                />
                {(form.phones || []).length > 1 && (
                  <button type="button" className="ghost-button danger" onClick={() => removeListItem('phones', index)}>Remove</button>
                )}
              </div>
            ))}
          </div>

          <div className="modal-actions">
            <button type="button" className="secondary-button" onClick={onClose}>Cancel</button>
            <button type="submit" className="primary-action">{contact ? 'Save changes' : 'Create contact'}</button>
          </div>
        </form>
      </div>
    </div>
  )
}
