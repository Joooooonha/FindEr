package com.finder.hospital.service;

import com.finder.common.exception.NotFoundException;
import com.finder.hospital.client.EGenApiClient;
import com.finder.hospital.domain.Hospital;
import com.finder.hospital.dto.HospitalDetailResponse;
import com.finder.hospital.dto.HospitalListResponse;
import com.finder.hospital.dto.HospitalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/** 응급실 조회 유스케이스. 병상 현황은 API 승인 후 추가 예정. */
@Service
@RequiredArgsConstructor
public class HospitalService {

    private static final int MAX_FETCH_COUNT = 100;

    private final EGenApiClient eGenApiClient;

    /** 위치 기반으로 반경 내 응급실 목록을 거리순으로 반환한다. */
    public HospitalListResponse getHospitals(double lat, double lng, double radiusKm) {
        List<HospitalResponse> hospitals = eGenApiClient.getHospitalsByLocation(lat, lng, MAX_FETCH_COUNT)
                .stream()
                .map(Hospital::new)
                .filter(h -> h.distanceTo(lat, lng) <= radiusKm)
                .sorted(Comparator.comparingDouble(h -> h.distanceTo(lat, lng)))
                .map(h -> HospitalResponse.of(h, h.distanceTo(lat, lng)))
                .toList();

        return new HospitalListResponse(hospitals);
    }

    /** 기관 ID로 응급실 상세 정보를 반환한다. */
    public HospitalDetailResponse getHospitalDetail(String hospitalId) {
        return eGenApiClient.getHospitalById(hospitalId)
                .map(Hospital::new)
                .map(HospitalDetailResponse::from)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 병원입니다."));
    }
}
