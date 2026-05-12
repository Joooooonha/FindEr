package com.finder.hospital.dto;

import com.finder.hospital.domain.BedSnapshot;
import com.finder.hospital.domain.BlockMessage;
import com.finder.hospital.domain.HospitalInfo;
import com.finder.hospital.domain.HospitalStatus;

import java.util.List;
import java.util.Set;

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
    public static HospitalDetailResponse from(
            HospitalInfo info,
            BedSnapshot bed,
            int staleThresholdMinutes,
            List<BlockMessage> activeBlockMessages,
            Set<String> availableTreatmentCodes
    ) {
        HospitalStatus status = bed != null ? bed.toStatus(staleThresholdMinutes) : HospitalStatus.UNKNOWN;
        Integer beds = bed != null ? bed.availableEmergencyBeds() : null;
        boolean stale = bed == null || bed.isStale(staleThresholdMinutes);
        String updatedAt = bed != null && bed.updatedAt() != null ? bed.updatedAt().toString() : null;
        return new HospitalDetailResponse(
                info.id(),
                info.name(),
                info.address(),
                info.phone(),
                status,
                beds,
                bed != null ? bed.operatingRooms() : null,
                bed != null ? bed.generalWardBeds() : null,
                bed != null ? bed.generalIcuBeds() : null,
                bed != null ? bed.neuroIcuBeds() : null,
                bed != null ? bed.emergencyIcuBeds() : null,
                bed != null && bed.operatingRooms() != null ? bed.operatingRooms() > 0 : info.surgeryAvailable(),
                bed != null ? bed.ctAvailable() : info.ctAvailable(),
                bed != null ? bed.mriAvailable() : info.mriAvailable(),
                bed != null && bed.ventilatorAvailable(),
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
