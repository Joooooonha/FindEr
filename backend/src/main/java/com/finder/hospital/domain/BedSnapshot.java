package com.finder.hospital.domain;

import com.finder.hospital.client.BedInfoItem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 특정 병원의 실시간 병상 스냅샷. 캐시에 저장되는 단위. */
public record BedSnapshot(
        Integer availableEmergencyBeds,
        LocalDateTime updatedAt
) {

    private static final DateTimeFormatter API_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]");

    public static BedSnapshot from(BedInfoItem item) {
        LocalDateTime updatedAt = parseDate(item.getModifiedAt());
        return new BedSnapshot(item.getEmro(), updatedAt);
    }

    /** 음수·null·갱신 30분 초과 시 데이터 신뢰 불가로 판단한다. */
    public HospitalStatus toStatus(int staleThresholdMinutes) {
        if (availableEmergencyBeds == null || availableEmergencyBeds < 0) return HospitalStatus.UNKNOWN;
        if (updatedAt == null) return HospitalStatus.UNKNOWN;
        if (Duration.between(updatedAt, LocalDateTime.now()).toMinutes() > staleThresholdMinutes) {
            return HospitalStatus.UNKNOWN;
        }
        if (availableEmergencyBeds == 0) return HospitalStatus.RED;
        if (availableEmergencyBeds <= 3) return HospitalStatus.YELLOW;
        return HospitalStatus.GREEN;
    }

    private static LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value.trim(), API_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }
}
