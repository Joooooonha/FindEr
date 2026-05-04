import { useParams } from 'react-router-dom'

export default function CardEditPage() {
  const { token } = useParams()
  return (
    <div style={{ padding: '32px 16px', textAlign: 'center', color: '#6b7280' }}>
      카드 수정 페이지 (구현 예정) — token: {token}
    </div>
  )
}
