import { useState } from 'react'
import TagInput from './TagInput'

const BLOOD_TYPES = ['', 'A+', 'A-', 'B+', 'B-', 'O+', 'O-', 'AB+', 'AB-']

const inputStyle = {
  width: '100%',
  padding: '8px 10px',
  fontSize: '14px',
  border: '1px solid #d1d5db',
  borderRadius: '6px',
  outline: 'none',
  boxSizing: 'border-box',
}

const labelStyle = {
  display: 'block',
  fontSize: '13px',
  fontWeight: 500,
  color: '#374151',
  marginBottom: '6px',
}

const fieldStyle = { marginBottom: '14px' }

/** 카드 생성/수정 공용 폼. requirePin=true면 PIN 필드 노출 (생성 모드). */
export default function CardForm({ initial, onSubmit, submitLabel, requirePin = false, submitting }) {
  const [form, setForm] = useState({
    name: initial?.name || '',
    birthDate: initial?.birthDate || '',
    bloodType: initial?.bloodType || '',
    allergies: initial?.allergies || [],
    medications: initial?.medications || [],
    conditions: initial?.conditions || [],
    surgeries: initial?.surgeries || [],
    guardianName: initial?.guardianName || '',
    guardianPhone: initial?.guardianPhone || '',
    isPregnant: initial?.isPregnant || false,
    pin: '',
  })

  const update = (key) => (e) => setForm(f => ({ ...f, [key]: e.target.value }))
  const updateTag = (key) => (tags) => setForm(f => ({ ...f, [key]: tags }))

  const handleSubmit = (e) => {
    e.preventDefault()
    if (!form.name.trim()) { alert('이름을 입력해주세요'); return }
    if (requirePin && (form.pin.length < 4 || form.pin.length > 8)) {
      alert('PIN은 4~8자로 입력해주세요'); return
    }
    onSubmit({
      name: form.name.trim(),
      birthDate: form.birthDate || null,
      bloodType: form.bloodType || null,
      allergies: form.allergies,
      medications: form.medications,
      conditions: form.conditions,
      surgeries: form.surgeries,
      guardianName: form.guardianName.trim() || null,
      guardianPhone: form.guardianPhone.trim() || null,
      isPregnant: form.isPregnant,
      ...(requirePin ? { pin: form.pin } : {}),
    })
  }

  return (
    <form onSubmit={handleSubmit}>
      <div style={fieldStyle}>
        <label style={labelStyle}>이름 <span style={{ color: '#dc2626' }}>*</span></label>
        <input type="text" value={form.name} onChange={update('name')} style={inputStyle} required />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '14px' }}>
        <div>
          <label style={labelStyle}>생년월일</label>
          <input type="date" value={form.birthDate} onChange={update('birthDate')} style={inputStyle} />
        </div>
        <div>
          <label style={labelStyle}>혈액형</label>
          <select value={form.bloodType} onChange={update('bloodType')} style={inputStyle}>
            {BLOOD_TYPES.map(t => <option key={t} value={t}>{t || '선택'}</option>)}
          </select>
        </div>
      </div>

      <TagInput label="알러지"   tags={form.allergies}   onChange={updateTag('allergies')}   placeholder="엔터로 추가" />
      <TagInput label="복용약"   tags={form.medications} onChange={updateTag('medications')} placeholder="엔터로 추가" />
      <TagInput label="기저질환" tags={form.conditions}  onChange={updateTag('conditions')}  placeholder="엔터로 추가" />
      <TagInput label="수술이력" tags={form.surgeries}   onChange={updateTag('surgeries')}   placeholder="엔터로 추가" />

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '14px' }}>
        <div>
          <label style={labelStyle}>보호자 이름</label>
          <input type="text" value={form.guardianName} onChange={update('guardianName')} style={inputStyle} />
        </div>
        <div>
          <label style={labelStyle}>보호자 전화번호</label>
          <input type="tel" value={form.guardianPhone} onChange={update('guardianPhone')} style={inputStyle} placeholder="010-1234-5678" />
        </div>
      </div>

      <div style={fieldStyle}>
        <label style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: '#374151' }}>
          <input
            type="checkbox"
            checked={form.isPregnant}
            onChange={e => setForm(f => ({ ...f, isPregnant: e.target.checked }))}
          />
          임신 중
        </label>
      </div>

      {requirePin && (
        <div style={fieldStyle}>
          <label style={labelStyle}>PIN <span style={{ color: '#dc2626' }}>*</span></label>
          <input
            type="password"
            value={form.pin}
            onChange={update('pin')}
            style={inputStyle}
            minLength={4}
            maxLength={8}
            required
            placeholder="4~8자 (수정·삭제 시 필요)"
          />
          <p style={{ fontSize: '12px', color: '#9ca3af', marginTop: '4px' }}>
            PIN을 잃어버리면 카드 수정·삭제가 불가합니다
          </p>
        </div>
      )}

      <button
        type="submit"
        disabled={submitting}
        style={{
          width: '100%',
          padding: '12px',
          fontSize: '15px',
          fontWeight: 600,
          color: '#fff',
          background: submitting ? '#9ca3af' : '#dc2626',
          border: 'none',
          borderRadius: '6px',
          cursor: submitting ? 'not-allowed' : 'pointer',
          marginTop: '8px',
        }}
      >
        {submitting ? '처리 중...' : submitLabel}
      </button>
    </form>
  )
}
