package com.finder.hospital.dto;

import com.finder.hospital.domain.BedSnapshot;
import com.finder.hospital.domain.BlockMessage;
import com.finder.hospital.domain.Hospital;
import com.finder.hospital.domain.HospitalStatus;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public record HospitalResponse(
        String id,
        String name,
        String address,
        String phone,
        double distance,
        HospitalStatus status,
        Integer availableBeds,
        String updatedAt,
        double lat,
        double lng,
        List<BlockMessageResponse> blockMessages,
        List<String> availableTreatments
) {
    public static HospitalResponse of(
            Hospital hospital,
            double distance,
            BedSnapshot bed,
            int staleThresholdMinutes,
            List<BlockMessage> activeBlockMessages,
            Set<String> availableTreatmentCodes
    ) {
        HospitalStatus status = bed != null ? bed.toStatus(staleThresholdMinutes) : HospitalStatus.UNKNOWN;
        Integer beds = bed != null ? bed.availableEmergencyBeds() : null;
        String updatedAt = bed != null && bed.updatedAt() != null ? bed.updatedAt().toString() : null;
        return new HospitalResponse(
                hospital.getId(),
                hospital.getName(),
                hospital.getAddress(),
                hospital.getPhone(),
                Math.round(distance * 10.0) / 10.0,
                status,
                beds,
                updatedAt,
                hospital.getLat(),
                hospital.getLng(),
                BlockMessageResponse.fromList(activeBlockMessages),
                sortedList(availableTreatmentCodes)
        );
    }

    private static List<String> sortedList(Set<String> codes) {
        if (codes == null || codes.isEmpty()) return List.of();
        return new TreeSet<>(codes).stream().toList();
    }
}
