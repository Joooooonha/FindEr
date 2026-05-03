package com.finder.hospital.dto;

import com.finder.hospital.domain.Hospital;
import com.finder.hospital.domain.HospitalStatus;

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
        double lng
) {
    public static HospitalDetailResponse from(Hospital hospital) {
        return new HospitalDetailResponse(
                hospital.getId(),
                hospital.getName(),
                hospital.getAddress(),
                hospital.getPhone(),
                HospitalStatus.UNKNOWN,
                null,
                hospital.isSurgeryAvailable(),
                hospital.isCtAvailable(),
                hospital.isMriAvailable(),
                null,
                hospital.getLat(),
                hospital.getLng()
        );
    }
}
