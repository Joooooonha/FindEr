package com.finder.hospital.dto;

import com.finder.hospital.domain.Hospital;
import com.finder.hospital.domain.HospitalStatus;

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
        double lng
) {
    public static HospitalResponse of(Hospital hospital, double distance) {
        return new HospitalResponse(
                hospital.getId(),
                hospital.getName(),
                hospital.getAddress(),
                hospital.getPhone(),
                Math.round(distance * 10.0) / 10.0,
                HospitalStatus.UNKNOWN,
                null,
                null,
                hospital.getLat(),
                hospital.getLng()
        );
    }
}
