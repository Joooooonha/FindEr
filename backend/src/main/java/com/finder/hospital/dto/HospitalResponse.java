package com.finder.hospital.dto;

import com.finder.hospital.domain.BedSnapshot;
import com.finder.hospital.domain.BlockMessage;
import com.finder.hospital.domain.HospitalInfo;
import com.finder.hospital.domain.HospitalStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

/** 병원 목록 조회 응답 DTO. */
public record HospitalResponse(
        String id,
        String name,
        String address,
        String phone,
        double distance,
        HospitalStatus status,
        Integer availableBeds,
        boolean stale,
        String updatedAt,
        double lat,
        double lng,
        List<BlockMessageResponse> blockMessages,
        List<String> availableTreatments
) {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static HospitalResponse of(
            HospitalInfo info,
            double distance,
            BedSnapshot bed,
            int staleThresholdMinutes,
            List<BlockMessage> activeBlockMessages,
            Set<String> availableTreatmentCodes
    ) {
        LocalDateTime now = LocalDateTime.now(KST);
        boolean stale = bed == null || bed.isStale(staleThresholdMinutes, now);
        HospitalStatus status = bed != null ? bed.toStatus(staleThresholdMinutes, now) : HospitalStatus.UNKNOWN;
        Integer beds = status != HospitalStatus.UNKNOWN ? bed.availableEmergencyBeds() : null;
        String updatedAt = bed != null && bed.updatedAt() != null ? bed.updatedAt().toString() : null;
        return new HospitalResponse(
                info.id(),
                info.name(),
                info.address(),
                info.phone(),
                Math.round(distance * 10.0) / 10.0,
                status,
                beds,
                stale,
                updatedAt,
                info.lat(),
                info.lng(),
                BlockMessageResponse.fromList(activeBlockMessages),
                sortedList(availableTreatmentCodes)
        );
    }

    private static List<String> sortedList(Set<String> codes) {
        if (codes == null || codes.isEmpty()) return List.of();
        return codes.stream().sorted().toList();
    }
}
