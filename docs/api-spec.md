# API 명세

Base URL: `/api/v1`

---

## 응급실

### 근처 응급실 목록 조회
```
GET /api/v1/hospitals?lat={위도}&lng={경도}&radius={반경km}&type={all|child|night}
```

**Response**
```json
{
  "hospitals": [
    {
      "id": "A1234",
      "name": "강남세브란스 응급실",
      "address": "서울 강남구 ...",
      "phone": "02-2019-3114",
      "distance": 2.3,
      "status": "GREEN",        // GREEN | YELLOW | RED | UNKNOWN
      "availableBeds": 12,      // null 가능 (데이터 없음)
      "updatedAt": "2024-09-17T03:22:00",
      "lat": 37.123,
      "lng": 127.456
    }
  ]
}
```

### 응급실 상세 조회
```
GET /api/v1/hospitals/{hospitalId}
```

**Response**
```json
{
  "id": "A1234",
  "name": "강남세브란스 응급실",
  "address": "서울 강남구 ...",
  "phone": "02-2019-3114",
  "status": "GREEN",
  "availableBeds": 12,
  "specialties": ["내과", "외과", "신경과", "소아과"],
  "surgeryAvailable": true,
  "ctAvailable": true,
  "mriAvailable": false,
  "updatedAt": "2024-09-17T03:22:00",
  "lat": 37.123,
  "lng": 127.456
}
```

