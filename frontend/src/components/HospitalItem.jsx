import StatusBadge from './StatusBadge'

export default function HospitalItem({ hospital, isSelected, onClick }) {
  return (
    <div
      onClick={onClick}
      style={{
        padding: '14px 16px',
        borderBottom: '1px solid #f3f4f6',
        cursor: 'pointer',
        background: isSelected ? '#eff6ff' : '#fff',
        borderLeft: `3px solid ${isSelected ? '#3b82f6' : 'transparent'}`,
        transition: 'background 0.15s',
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '8px' }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <p style={{ fontSize: '14px', fontWeight: 600, color: '#111827', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {hospital.name}
          </p>
          <p style={{ fontSize: '12px', color: '#9ca3af', marginTop: '2px' }}>
            {hospital.distance}km
          </p>
        </div>
        <StatusBadge status={hospital.status} />
      </div>
      {hospital.phone && (
        <a
          href={`tel:${hospital.phone}`}
          onClick={e => e.stopPropagation()}
          style={{ display: 'inline-block', marginTop: '8px', fontSize: '12px', color: '#3b82f6', textDecoration: 'none' }}
        >
          {hospital.phone}
        </a>
      )}
    </div>
  )
}
