package com.finder.hospital.dto;

import com.finder.hospital.domain.BedSnapshot;
import com.finder.hospital.domain.BlockMessage;
import com.finder.hospital.domain.Hospital;
import com.finder.hospital.domain.HospitalStatus;

import java.util.List;

public record HospitalDetailResponse(
        String id,
        String name,
        String address,
        String phone,
        HospitalStatus status,
        Integer availableBeds,
        boolean surgeryAvailable,
        boolean ctAvailable,
        boolean mriAvailable,
        String updatedAt,
        double lat,
        double lng,
        List<BlockMessageResponse> blockMessages
) {
    public static HospitalDetailResponse from(
            Hospital hospital,
            BedSnapshot bed,
            int staleThresholdMinutes,
            List<BlockMessage> activeBlockMessages
    ) {
        HospitalStatus status = bed != null ? bed.toStatus(staleThresholdMinutes) : HospitalStatus.UNKNOWN;
        Integer beds = bed != null ? bed.availableEmergencyBeds() : null;
        String updatedAt = bed != null && bed.updatedAt() != null ? bed.updatedAt().toString() : null;
        return new HospitalDetailResponse(
                hospital.getId(),
                hospital.getName(),
                hospital.getAddress(),
                hospital.getPhone(),
                status,
                beds,
                hospital.isSurgeryAvailable(),
                hospital.isCtAvailable(),
                hospital.isMriAvailable(),
                updatedAt,
                hospital.getLat(),
                hospital.getLng(),
                BlockMessageResponse.fromList(activeBlockMessages)
        );
    }
}
