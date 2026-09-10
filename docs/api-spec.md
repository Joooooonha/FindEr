# 내부 API 명세

Base URL: `/api/v1`

모든 병상 관련 값은 공공 API의 스냅샷입니다. `stale: true`이거나 `status: "UNKNOWN"`이면 병상 수는 `null`이며 실제 수용 가능 여부를 뜻하지 않습니다.

## 근처 응급실 목록

```http
GET /api/v1/hospitals?lat=37.5665&lng=126.9780&radius=5
```

### Query parameters

| 이름 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---:|---|
| `lat` | number | O | - | 기준 위도 |
| `lng` | number | O | - | 기준 경도 |
| `radius` | number | X | `5.0` | 검색 반경(km) |

현재 백엔드는 `type` 필터를 받지 않습니다. 증상, 가용 병상, 갱신 시각 필터와 정렬은 목록 응답을 받은 프론트엔드에서 적용합니다.

### Response `200`

```json
{
  "hospitals": [
    {
      "id": "A1234",
      "name": "테스트 응급실",
      "address": "서울시 테스트구",
      "phone": "02-0000-0000",
      "distance": 2.3,
      "status": "GREEN",
      "availableBeds": 5,
      "stale": false,
      "updatedAt": "2026-09-10T11:50:00",
      "lat": 37.55,
      "lng": 126.98,
      "blockMessages": [
        {
          "message": "수용 제한 안내",
          "messageType": "응급실",
          "diseaseTypeName": "중증질환"
        }
      ],
      "availableTreatments": ["mkioskty1", "mkioskty2"]
    }
  ]
}
```

`distance`는 Haversine 거리의 km 값을 소수점 첫째 자리로 반올림합니다. 목록은 거리 오름차순입니다.

## 응급실 상세

```http
GET /api/v1/hospitals/{hospitalId}
```

### Path parameter

| 이름 | 타입 | 설명 |
|---|---|---|
| `hospitalId` | string | 공공데이터의 병원 식별자 `hpid` |

### Response `200`

```json
{
  "id": "A1234",
  "name": "테스트 응급실",
  "address": "서울시 테스트구",
  "phone": "02-0000-0000",
  "status": "YELLOW",
  "availableBeds": 2,
  "operatingRooms": 1,
  "generalWardBeds": 7,
  "generalIcuBeds": 3,
  "neuroIcuBeds": 1,
  "emergencyIcuBeds": 1,
  "surgeryAvailable": true,
  "ctAvailable": true,
  "mriAvailable": false,
  "ventilatorAvailable": true,
  "stale": false,
  "updatedAt": "2026-09-10T11:50:00",
  "lat": 37.55,
  "lng": 126.98,
  "blockMessages": [],
  "availableTreatments": ["mkioskty1"]
}
```

### Response `404`

```json
{
  "code": "NOT_FOUND",
  "message": "존재하지 않는 병원입니다."
}
```

## 상태 규칙

| `status` | 기준 |
|---|---|
| `GREEN` | 가용 응급실 병상 4개 이상 |
| `YELLOW` | 1–3개 |
| `RED` | 0개 |
| `UNKNOWN` | 데이터 없음, 갱신 시각 없음, 음수 값, 또는 30분 초과 |

`UNKNOWN`에서는 목록의 `availableBeds`와 상세의 병상·입원실·중환자실 수치가 모두 `null`입니다. `updatedAt`은 사용자가 오래된 정도를 확인할 수 있도록 원래 값을 유지합니다. 장비 관련 불리언은 유효한 병상 스냅샷이 없을 때 E-Gen 기관 기본정보를 사용합니다.
