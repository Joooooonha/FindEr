package com.finder.hospital.domain;

/** 응급실 가용 상태. 병상 수 기준으로 분류한다. */
public enum HospitalStatus {
    GREEN,    // 여유
    YELLOW,   // 보통
    RED,      // 혼잡
    UNKNOWN   // 정보 없음 (실시간 API 미승인 또는 데이터 오류)
}
