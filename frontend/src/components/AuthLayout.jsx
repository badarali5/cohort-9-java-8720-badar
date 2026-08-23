import { Link } from 'react-router-dom'
import { ContactRound } from 'lucide-react'

export default function AuthLayout({ eyebrow, title, description, children, footer, footerLink, footerText }) {
  return (
    <main className="auth-shell">
      <section className="auth-art" aria-label="Contact management">
        <Link className="brand brand-light" to="/login"><ContactRound size={22} /> Kinfolk</Link>
        <div className="art-copy">
          <p className="eyebrow">{eyebrow}</p>
          <h1>{title}</h1>
          <p>{description}</p>
        </div>
        <div className="art-stamp">CONTACTS<br /><span>01 / 24</span></div>
      </section>
      <section className="auth-panel">
        <div className="auth-panel-inner">
          <div className="mobile-brand"><Link className="brand" to="/login"><ContactRound size={22} /> Kinfolk</Link></div>
          {children}
          <p className="auth-footer">{footerText} <Link to={footerLink}>{footer}</Link></p>
        </div>
      </section>
    </main>
  )
}
