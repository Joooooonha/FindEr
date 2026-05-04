import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      height: '100%',
      padding: '40px 16px',
      textAlign: 'center',
    }}>
      <h1 style={{ fontSize: '48px', fontWeight: 800, color: '#dc2626', margin: 0 }}>404</h1>
      <p style={{ fontSize: '15px', color: '#6b7280', marginTop: '8px', marginBottom: '24px' }}>
        요청하신 페이지를 찾을 수 없습니다
      </p>
      <Link
        to="/"
        style={{
          padding: '10px 20px',
          fontSize: '14px',
          fontWeight: 600,
          color: '#fff',
          background: '#dc2626',
          borderRadius: '6px',
          textDecoration: 'none',
        }}
      >
        지도로 돌아가기
      </Link>
    </div>
  )
}
