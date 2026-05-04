import { useParams } from 'react-router-dom'

export default function CardViewPage() {
  const { token } = useParams()
  return (
    <div style={{ padding: '32px 16px', textAlign: 'center', color: '#6b7280' }}>
      카드 조회 페이지 (구현 예정) — token: {token}
    </div>
  )
}
