package com.finder.hospital.service;

import com.finder.common.exception.NotFoundException;
import com.finder.hospital.client.EGenApiClient;
import com.finder.hospital.domain.HospitalInfo;
import com.finder.hospital.dto.HospitalDetailResponse;
import com.finder.hospital.dto.HospitalListResponse;
import com.finder.hospital.dto.HospitalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** 응급실 조회 유스케이스. 마스터 캐시 + 실시간 병상 + 차단메시지 + 가용 시술 결합. */
@Service
@RequiredArgsConstructor
public class HospitalService {

    private static final int FALLBACK_FETCH_COUNT = 100;

    private final EGenApiClient eGenApiClient;
    private final HospitalInfoCache hospitalInfoCache;
    private final BedInfoCache bedInfoCache;
    private final BlockMessageCache blockMessageCache;
    private final SeverePossibilityCache severePossibilityCache;

    @Value("${bed.api.stale-threshold-minutes}")
    private int staleThresholdMinutes;

    /** 위치 기반으로 반경 내 응급실 목록을 거리순으로 반환한다. 마스터 캐시에서 거리 계산하므로 외부 API 호출 0회. */
    public HospitalListResponse getHospitals(double lat, double lng, double radiusKm) {
        Collection<HospitalInfo> source = resolveSource(lat, lng);

        List<HospitalResponse> hospitals = source.stream()
                .filter(h -> h.distanceTo(lat, lng) <= radiusKm)
                .sorted(Comparator.comparingDouble(h -> h.distanceTo(lat, lng)))
                .map(h -> HospitalResponse.of(
                        h,
                        h.distanceTo(lat, lng),
                        bedInfoCache.find(h.id()).orElse(null),
                        staleThresholdMinutes,
                        blockMessageCache.findActive(h.id()),
                        severePossibilityCache.findCodes(h.id())))
                .toList();

        return new HospitalListResponse(hospitals);
    }

    /** 기관 ID로 응급실 상세 정보를 반환한다. */
    public HospitalDetailResponse getHospitalDetail(String hospitalId) {
        HospitalInfo info = hospitalInfoCache.find(hospitalId)
                .or(() -> eGenApiClient.getHospitalById(hospitalId).map(HospitalInfo::from))
                .orElseThrow(() -> new NotFoundException("존재하지 않는 병원입니다."));

        return HospitalDetailResponse.from(
                info,
                bedInfoCache.find(info.id()).orElse(null),
                staleThresholdMinutes,
                blockMessageCache.findActive(info.id()),
                severePossibilityCache.findCodes(info.id())
        );
    }

    /** 마스터 캐시가 워밍업 안 됐을 때만 위치 API로 폴백한다. */
    private Collection<HospitalInfo> resolveSource(double lat, double lng) {
        if (!hospitalInfoCache.isEmpty()) return hospitalInfoCache.all();
        return eGenApiClient.getHospitalsByLocation(lat, lng, FALLBACK_FETCH_COUNT)
                .stream()
                .map(HospitalInfo::from)
                .toList();
    }
}
