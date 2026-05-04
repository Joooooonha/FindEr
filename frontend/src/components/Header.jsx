import { Link, useNavigate } from 'react-router-dom'
import { getMyCardToken } from '../api/cardStorage'

export default function Header() {
  const navigate = useNavigate()

  const handleCardClick = () => {
    const token = getMyCardToken()
    navigate(token ? `/cards/${token}` : '/cards/new')
  }

  return (
    <header style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '10px 16px',
      borderBottom: '1px solid #e5e7eb',
      background: '#fff',
      flexShrink: 0,
    }}>
      <Link to="/" style={{ fontSize: '16px', fontWeight: 700, color: '#dc2626', textDecoration: 'none' }}>
        FindEr
      </Link>
      <button
        onClick={handleCardClick}
        style={{
          padding: '6px 12px',
          fontSize: '13px',
          fontWeight: 500,
          color: '#111827',
          background: '#f3f4f6',
          border: '1px solid #e5e7eb',
          borderRadius: '6px',
          cursor: 'pointer',
        }}
      >
        응급카드
      </button>
    </header>
  )
}
