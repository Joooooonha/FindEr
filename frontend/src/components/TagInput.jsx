import { useState } from 'react'

export default function TagInput({ label, tags, onChange, placeholder }) {
  const [draft, setDraft] = useState('')

  const addTag = () => {
    const v = draft.trim()
    if (!v) return
    if (tags.includes(v)) { setDraft(''); return }
    onChange([...tags, v])
    setDraft('')
  }

  const removeTag = (target) => onChange(tags.filter(t => t !== target))

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault()
      addTag()
    } else if (e.key === 'Backspace' && !draft && tags.length > 0) {
      onChange(tags.slice(0, -1))
    }
  }

  // × 버튼 클릭 시 발생하는 blur 이벤트가 addTag를 호출해 의도치 않은 추가가 일어나지 않도록 필터링.
  const handleBlur = (e) => {
    if (e.relatedTarget?.dataset?.removeTag) return
    addTag()
  }

  return (
    <div style={{ marginBottom: '14px' }}>
      <label style={{ display: 'block', fontSize: '13px', fontWeight: 500, color: '#374151', marginBottom: '6px' }}>
        {label}
      </label>
      <div style={{
        display: 'flex',
        flexWrap: 'wrap',
        gap: '6px',
        padding: '8px',
        border: '1px solid #d1d5db',
        borderRadius: '6px',
        background: '#fff',
        minHeight: '40px',
      }}>
        {tags.map(tag => (
          <span key={tag} style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '4px',
            padding: '3px 8px',
            background: '#eff6ff',
            color: '#1e40af',
            borderRadius: '12px',
            fontSize: '12px',
          }}>
            {tag}
            <button
              type="button"
              data-remove-tag="true"
              onClick={() => removeTag(tag)}
              style={{ border: 'none', background: 'none', cursor: 'pointer', color: '#1e40af', fontSize: '14px', lineHeight: 1, padding: 0 }}
              aria-label="삭제"
            >
              ×
            </button>
          </span>
        ))}
        <input
          type="text"
          value={draft}
          onChange={e => setDraft(e.target.value)}
          onKeyDown={handleKeyDown}
          onBlur={handleBlur}
          placeholder={tags.length === 0 ? placeholder : ''}
          style={{ flex: 1, minWidth: '100px', border: 'none', outline: 'none', fontSize: '13px' }}
        />
      </div>
    </div>
  )
}
