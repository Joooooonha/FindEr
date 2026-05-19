export default function HelpBadge({ label }) {
  return (
    <span
      aria-label={label}
      title={label}
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: '16px',
        height: '16px',
        borderRadius: '50%',
        border: '1px solid #cbd5e1',
        color: '#64748b',
        fontSize: '11px',
        fontWeight: 700,
        cursor: 'help',
        lineHeight: 1,
      }}
    >
      ?
    </span>
  )
}
