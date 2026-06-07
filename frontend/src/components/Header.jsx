import { Link } from 'react-router-dom'

export default function Header() {
  return (
    <header style={{
      display: 'flex',
      alignItems: 'center',
      padding: '10px 16px',
      borderBottom: '1px solid #e5e7eb',
      background: '#fff',
      flexShrink: 0,
    }}>
      <Link to="/" style={{ fontSize: '16px', fontWeight: 700, color: '#dc2626', textDecoration: 'none' }}>
        FindEr
      </Link>
    </header>
  )
}
