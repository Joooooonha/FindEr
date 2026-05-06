package com.finder.hospital.service;

import com.finder.common.exception.NotFoundException;
import com.finder.hospital.client.EGenApiClient;
import com.finder.hospital.domain.Hospital;
import com.finder.hospital.dto.HospitalDetailResponse;
import com.finder.hospital.dto.HospitalListResponse;
import com.finder.hospital.dto.HospitalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/** 응급실 조회 유스케이스. 위치 기반 기관 정보 + 실시간 병상 + 차단메시지 + 가용 시술 결합. */
@Service
@RequiredArgsConstructor
public class HospitalService {

    private static final int MAX_FETCH_COUNT = 100;

    private final EGenApiClient eGenApiClient;
    private final BedInfoCache bedInfoCache;
    private final BlockMessageCache blockMessageCache;
    private final SeverePossibilityCache severePossibilityCache;

    @Value("${bed.api.stale-threshold-minutes}")
    private int staleThresholdMinutes;

    /** 위치 기반으로 반경 내 응급실 목록을 거리순으로 반환한다. */
    public HospitalListResponse getHospitals(double lat, double lng, double radiusKm) {
        List<HospitalResponse> hospitals = eGenApiClient.getHospitalsByLocation(lat, lng, MAX_FETCH_COUNT)
                .stream()
                .map(Hospital::new)
                .filter(h -> h.distanceTo(lat, lng) <= radiusKm)
                .sorted(Comparator.comparingDouble(h -> h.distanceTo(lat, lng)))
                .map(h -> HospitalResponse.of(
                        h,
                        h.distanceTo(lat, lng),
                        bedInfoCache.find(h.getId()).orElse(null),
                        staleThresholdMinutes,
                        blockMessageCache.findActive(h.getId()),
                        severePossibilityCache.findCodes(h.getId())))
                .toList();

        return new HospitalListResponse(hospitals);
    }

    /** 기관 ID로 응급실 상세 정보를 반환한다. */
    public HospitalDetailResponse getHospitalDetail(String hospitalId) {
        return eGenApiClient.getHospitalById(hospitalId)
                .map(Hospital::new)
                .map(h -> HospitalDetailResponse.from(
                        h,
                        bedInfoCache.find(h.getId()).orElse(null),
                        staleThresholdMinutes,
                        blockMessageCache.findActive(h.getId()),
                        severePossibilityCache.findCodes(h.getId())))
                .orElseThrow(() -> new NotFoundException("존재하지 않는 병원입니다."));
    }
}
