import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import CardForm from '../components/CardForm'
import { fetchCard, updateCard, deleteCard } from '../api/card'
import { clearMyCardToken } from '../api/cardStorage'

export default function CardEditPage() {
  const { token } = useParams()
  const navigate = useNavigate()
  const [card, setCard] = useState(null)
  const [pin, setPin] = useState('')
  const [pinVerified, setPinVerified] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetchCard(token)
      .then(setCard)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [token])

  const handleVerify = (e) => {
    e.preventDefault()
    if (pin.length < 4 || pin.length > 8) {
      alert('PIN은 4~8자입니다'); return
    }
    setPinVerified(true)
  }

  const handleUpdate = async (payload) => {
    setSubmitting(true)
    try {
      await updateCard(token, pin, payload)
      navigate(`/cards/${token}`)
    } catch (err) {
      alert(err.message || '수정 실패')
      setSubmitting(false)
    }
  }

  const handleDelete = async () => {
    if (!confirm('정말 카드를 삭제하시겠습니까? 삭제 후 복구할 수 없습니다.')) return
    setSubmitting(true)
    try {
      await deleteCard(token, pin)
      clearMyCardToken()
      alert('카드가 삭제되었습니다.')
      navigate('/', { replace: true })
    } catch (err) {
      alert(err.message || '삭제 실패')
      setSubmitting(false)
    }
  }

  if (loading) return <div style={{ padding: '40px 16px', textAlign: 'center', color: '#9ca3af' }}>불러오는 중...</div>
  if (error) return <div style={{ padding: '40px 16px', textAlign: 'center', color: '#dc2626' }}>{error}</div>

  if (!pinVerified) {
    return (
      <div style={{ maxWidth: '400px', margin: '0 auto', padding: '40px 16px', textAlign: 'center' }}>
        <h1 style={{ fontSize: '18px', fontWeight: 700, color: '#111827', marginBottom: '8px' }}>PIN 입력</h1>
        <p style={{ fontSize: '13px', color: '#6b7280', marginBottom: '24px' }}>
          카드 수정·삭제를 위해 PIN을 입력해주세요
        </p>
        <form onSubmit={handleVerify}>
          <input
            type="password"
            value={pin}
            onChange={e => setPin(e.target.value)}
            minLength={4}
            maxLength={8}
            placeholder="4~8자"
            autoFocus
            style={{
              width: '100%',
              padding: '10px 12px',
              fontSize: '15px',
              border: '1px solid #d1d5db',
              borderRadius: '6px',
              outline: 'none',
              boxSizing: 'border-box',
              marginBottom: '12px',
              textAlign: 'center',
              letterSpacing: '4px',
            }}
          />
          <button
            type="submit"
            style={{
              width: '100%',
              padding: '12px',
              fontSize: '15px',
              fontWeight: 600,
              color: '#fff',
              background: '#dc2626',
              border: 'none',
              borderRadius: '6px',
              cursor: 'pointer',
            }}
          >
            확인
          </button>
        </form>
      </div>
    )
  }

  return (
    <div style={{ maxWidth: '560px', margin: '0 auto', padding: '24px 16px', overflowY: 'auto', height: '100%' }}>
      <h1 style={{ fontSize: '20px', fontWeight: 700, color: '#111827', marginBottom: '20px' }}>응급카드 수정</h1>
      <CardForm initial={card} onSubmit={handleUpdate} submitLabel="수정 저장" submitting={submitting} />

      <div style={{ marginTop: '24px', paddingTop: '20px', borderTop: '1px solid #e5e7eb' }}>
        <button
          type="button"
          onClick={handleDelete}
          disabled={submitting}
          style={{
            width: '100%',
            padding: '10px',
            fontSize: '13px',
            fontWeight: 500,
            color: '#dc2626',
            background: '#fff',
            border: '1px solid #fecaca',
            borderRadius: '6px',
            cursor: submitting ? 'not-allowed' : 'pointer',
          }}
        >
          카드 삭제
        </button>
        <p style={{ fontSize: '11px', color: '#9ca3af', textAlign: 'center', marginTop: '6px' }}>
          삭제하면 복구할 수 없습니다
        </p>
      </div>
    </div>
  )
}
