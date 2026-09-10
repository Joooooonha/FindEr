package com.finder.hospital.domain;

import com.finder.hospital.client.BedInfoItem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Snapshot of realtime capacity fields from the public bed API. */
public record BedSnapshot(
        Integer availableEmergencyBeds,
        Integer operatingRooms,
        Integer generalWardBeds,
        Integer generalIcuBeds,
        Integer neuroIcuBeds,
        Integer emergencyIcuBeds,
        boolean ctAvailable,
        boolean mriAvailable,
        boolean ventilatorAvailable,
        LocalDateTime updatedAt
) {

    private static final DateTimeFormatter API_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]");
    // 안전데이터의 MDFCN_DT는 한국 시각이며, EC2 기본 타임존은 UTC일 수 있다.
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static BedSnapshot from(BedInfoItem item) {
        return new BedSnapshot(
                item.getEmro(),
                item.getOpro(),
                item.getWard(),
                item.getGnrlIcu(),
                item.getNrvsIcu(),
                item.getEmergnIcu(),
                isAvailable(item.getCtAvailable()),
                isAvailable(item.getMriAvailable()),
                isAvailable(item.getVentAvailable()),
                parseDate(item.getModifiedAt())
        );
    }

    /** 병상 수와 갱신 시각을 함께 확인해 상태를 분류한다. */
    public HospitalStatus toStatus(int staleThresholdMinutes) {
        return toStatus(staleThresholdMinutes, LocalDateTime.now(KST));
    }

    /** 호출부와 테스트에서 동일한 기준 시각으로 상태를 분류한다. */
    public HospitalStatus toStatus(int staleThresholdMinutes, LocalDateTime now) {
        if (isStale(staleThresholdMinutes, now)) return HospitalStatus.UNKNOWN;
        if (availableEmergencyBeds == null || availableEmergencyBeds < 0) return HospitalStatus.UNKNOWN;
        if (availableEmergencyBeds == 0) return HospitalStatus.RED;
        if (availableEmergencyBeds <= 3) return HospitalStatus.YELLOW;
        return HospitalStatus.GREEN;
    }

    public boolean isStale(int staleThresholdMinutes, LocalDateTime now) {
        if (updatedAt == null) return true;
        return Duration.between(updatedAt, now).toMinutes() > staleThresholdMinutes;
    }

    private static LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value.trim(), API_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isAvailable(String value) {
        return value != null && value.trim().equalsIgnoreCase("Y");
    }
}
