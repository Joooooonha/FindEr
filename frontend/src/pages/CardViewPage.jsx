import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { QRCodeSVG } from 'qrcode.react'
import { fetchCard } from '../api/card'
import { getMyCardToken } from '../api/cardStorage'

const labelStyle = {
  fontSize: '12px',
  color: '#6b7280',
  fontWeight: 500,
  textTransform: 'uppercase',
  letterSpacing: '0.5px',
}

const valueStyle = { fontSize: '14px', color: '#111827', marginTop: '2px' }

const sectionStyle = {
  padding: '14px 16px',
  borderBottom: '1px solid #f3f4f6',
}

function Section({ label, value }) {
  if (!value || (Array.isArray(value) && value.length === 0)) return null
  return (
    <div style={sectionStyle}>
      <div style={labelStyle}>{label}</div>
      <div style={valueStyle}>
        {Array.isArray(value) ? value.join(', ') : value}
      </div>
    </div>
  )
}

export default function CardViewPage() {
  const { token } = useParams()
  const [card, setCard] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    fetchCard(token)
      .then(setCard)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [token])

  const isMine = getMyCardToken() === token
  const shareUrl = `${window.location.origin}/cards/${token}`

  if (loading) {
    return <div style={{ padding: '40px 16px', textAlign: 'center', color: '#9ca3af' }}>불러오는 중...</div>
  }

  if (error) {
    return (
      <div style={{ padding: '40px 16px', textAlign: 'center' }}>
        <p style={{ color: '#dc2626', marginBottom: '16px' }}>{error}</p>
        <Link to="/cards/new" style={{ color: '#3b82f6', fontSize: '14px' }}>카드 새로 만들기</Link>
      </div>
    )
  }

  return (
    <div style={{ maxWidth: '560px', margin: '0 auto', padding: '24px 16px', overflowY: 'auto', height: '100%' }}>
      <div style={{
        background: '#fff',
        border: '1px solid #e5e7eb',
        borderRadius: '12px',
        overflow: 'hidden',
        marginBottom: '20px',
      }}>
        <div style={{ padding: '20px 16px', background: '#fef2f2', borderBottom: '1px solid #fecaca' }}>
          <p style={{ fontSize: '11px', fontWeight: 700, color: '#991b1b', letterSpacing: '1px' }}>EMERGENCY CARD</p>
          <h1 style={{ fontSize: '22px', fontWeight: 800, color: '#111827', marginTop: '4px' }}>{card.name}</h1>
          {card.birthDate && <p style={{ fontSize: '13px', color: '#6b7280', marginTop: '2px' }}>{card.birthDate}</p>}
        </div>

        <Section label="혈액형" value={card.bloodType} />
        <Section label="알러지" value={card.allergies} />
        <Section label="복용약" value={card.medications} />
        <Section label="기저질환" value={card.conditions} />
        <Section label="수술이력" value={card.surgeries} />
        {card.isPregnant && <Section label="임신 여부" value="임신 중" />}
        {(card.guardianName || card.guardianPhone) && (
          <Section label="보호자" value={[card.guardianName, card.guardianPhone].filter(Boolean).join(' · ')} />
        )}
      </div>

      <div style={{
        background: '#fff',
        border: '1px solid #e5e7eb',
        borderRadius: '12px',
        padding: '20px',
        textAlign: 'center',
        marginBottom: '20px',
      }}>
        <p style={{ fontSize: '13px', color: '#6b7280', marginBottom: '12px' }}>
          QR 코드를 스캔하면 이 카드를 볼 수 있습니다
        </p>
        <div style={{ display: 'inline-block', padding: '12px', background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px' }}>
          <QRCodeSVG value={shareUrl} size={160} level="M" title="응급카드 공유 QR 코드" />
        </div>
        <p style={{ fontSize: '11px', color: '#9ca3af', marginTop: '12px', wordBreak: 'break-all' }}>{shareUrl}</p>
      </div>

      {isMine && (
        <div style={{ display: 'flex', gap: '8px' }}>
          <Link
            to={`/cards/${token}/edit`}
            style={{
              flex: 1,
              padding: '12px',
              fontSize: '14px',
              fontWeight: 600,
              textAlign: 'center',
              color: '#111827',
              background: '#f3f4f6',
              border: '1px solid #e5e7eb',
              borderRadius: '6px',
              textDecoration: 'none',
            }}
          >
            수정
          </Link>
        </div>
      )}
    </div>
  )
}
