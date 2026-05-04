import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import CardForm from '../components/CardForm'
import { createCard } from '../api/card'
import { saveMyCardToken } from '../api/cardStorage'

export default function CardCreatePage() {
  const navigate = useNavigate()
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (payload) => {
    setSubmitting(true)
    try {
      const { token } = await createCard(payload)
      saveMyCardToken(token)
      navigate(`/cards/${token}`, { replace: true })
    } catch (err) {
      alert(err.message || '카드 생성 실패')
      setSubmitting(false)
    }
  }

  return (
    <div style={{ maxWidth: '560px', margin: '0 auto', padding: '24px 16px', overflowY: 'auto', height: '100%' }}>
      <h1 style={{ fontSize: '20px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>응급카드 생성</h1>
      <p style={{ fontSize: '13px', color: '#6b7280', marginBottom: '20px' }}>
        응급 상황 시 의료진이 빠르게 확인할 수 있도록 본인의 의료 정보를 등록하세요.
      </p>
      <CardForm onSubmit={handleSubmit} submitLabel="카드 생성" requirePin submitting={submitting} />
    </div>
  )
}
