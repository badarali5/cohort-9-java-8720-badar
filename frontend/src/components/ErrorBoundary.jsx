import React from 'react'

export default class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props)
    this.state = { hasError: false }
  }

  static getDerivedStateFromError() {
    return { hasError: true }
  }

  componentDidCatch(error, errorInfo) {
    console.error('App error boundary caught an error:', error, errorInfo)
  }

  render() {
    if (this.state.hasError) {
      return (
        <main className="dashboard error-boundary">
          <div className="empty-state" style={{ paddingTop: '120px' }}>
            <div className="empty-icon">!</div>
            <h2>Something went wrong</h2>
            <p>We hit an unexpected issue. Please refresh and try again.</p>
            <button
              type="button"
              className="primary-action"
              onClick={() => window.location.reload()}
            >
              Refresh page
            </button>
          </div>
        </main>
      )
    }

    return this.props.children
  }
}
