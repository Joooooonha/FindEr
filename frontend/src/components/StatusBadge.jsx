const STATUS_CONFIG = {
  GREEN:   { label: '여유',    bg: '#dcfce7', color: '#166534' },
  YELLOW:  { label: '보통',    bg: '#fef9c3', color: '#854d0e' },
  RED:     { label: '혼잡',    bg: '#fee2e2', color: '#991b1b' },
  UNKNOWN: { label: '정보없음', bg: '#f3f4f6', color: '#6b7280' },
}

export default function StatusBadge({ status }) {
  const cfg = STATUS_CONFIG[status] || STATUS_CONFIG.UNKNOWN
  return (
    <span style={{
      padding: '3px 8px',
      borderRadius: '12px',
      fontSize: '11px',
      fontWeight: 600,
      background: cfg.bg,
      color: cfg.color,
      whiteSpace: 'nowrap',
    }}>
      {cfg.label}
    </span>
  )
}
