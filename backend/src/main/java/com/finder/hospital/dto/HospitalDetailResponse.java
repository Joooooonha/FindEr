package com.finder.hospital.dto;

import com.finder.hospital.domain.BedSnapshot;
import com.finder.hospital.domain.BlockMessage;
import com.finder.hospital.domain.HospitalInfo;
import com.finder.hospital.domain.HospitalStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

/** 병원 상세 정보 응답 DTO. */
public record HospitalDetailResponse(
        String id,
        String name,
        String address,
        String phone,
        HospitalStatus status,
        Integer availableBeds,
        Integer operatingRooms,
        Integer generalWardBeds,
        Integer generalIcuBeds,
        Integer neuroIcuBeds,
        Integer emergencyIcuBeds,
        boolean surgeryAvailable,
        boolean ctAvailable,
        boolean mriAvailable,
        boolean ventilatorAvailable,
        boolean stale,
        String updatedAt,
        double lat,
        double lng,
        List<BlockMessageResponse> blockMessages,
        List<String> availableTreatments
) {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static HospitalDetailResponse from(
            HospitalInfo info,
            BedSnapshot bed,
            int staleThresholdMinutes,
            List<BlockMessage> activeBlockMessages,
            Set<String> availableTreatmentCodes
    ) {
        LocalDateTime now = LocalDateTime.now(KST);
        boolean stale = bed == null || bed.isStale(staleThresholdMinutes, now);
        HospitalStatus status = bed != null ? bed.toStatus(staleThresholdMinutes, now) : HospitalStatus.UNKNOWN;
        boolean usableBedData = bed != null && !stale && status != HospitalStatus.UNKNOWN;
        Integer beds = usableBedData ? bed.availableEmergencyBeds() : null;
        String updatedAt = bed != null && bed.updatedAt() != null ? bed.updatedAt().toString() : null;
        return new HospitalDetailResponse(
                info.id(),
                info.name(),
                info.address(),
                info.phone(),
                status,
                beds,
                usableBedData ? bed.operatingRooms() : null,
                usableBedData ? bed.generalWardBeds() : null,
                usableBedData ? bed.generalIcuBeds() : null,
                usableBedData ? bed.neuroIcuBeds() : null,
                usableBedData ? bed.emergencyIcuBeds() : null,
                usableBedData && bed.operatingRooms() != null
                        ? bed.operatingRooms() > 0
                        : info.surgeryAvailable(),
                usableBedData ? bed.ctAvailable() : info.ctAvailable(),
                usableBedData ? bed.mriAvailable() : info.mriAvailable(),
                usableBedData ? bed.ventilatorAvailable() : info.ventilatorAvailable(),
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
