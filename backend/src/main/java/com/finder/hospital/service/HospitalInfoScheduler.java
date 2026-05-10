package com.finder.hospital.service;

import com.finder.hospital.client.EGenApiClient;
import com.finder.hospital.client.EGenItem;
import com.finder.hospital.domain.HospitalInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** 응급의료기관 기본정보 주기적 폴링. 좌표·연락처는 거의 변하지 않아 24시간 주기로 충분. */
@Component
@RequiredArgsConstructor
public class HospitalInfoScheduler {

    private static final Logger log = LoggerFactory.getLogger(HospitalInfoScheduler.class);

    private final EGenApiClient eGenApiClient;
    private final HospitalInfoCache cache;

    /**
     * 응급실 hpid 화이트리스트 → 단건 기본정보 조회로 좌표·주소를 보강해 캐시한다.
     * 기본정보 API 페이지네이션은 일반 의원까지 10만 건 이상 반환해 응급실 식별이 불가하므로
     * 실시간 가용병상 API의 hpid 셋을 출처로 삼는다.
     */
    @Scheduled(fixedDelayString = "${hospital.info.refresh-interval-ms}")
    public void refresh() {
        Set<String> hpids = eGenApiClient.getEmergencyHpids();
        if (hpids.isEmpty()) {
            log.warn("응급실 hpid 화이트리스트 조회 실패 또는 비어있음. 기존 캐시 유지");
            return;
        }

        Map<String, HospitalInfo> snapshot = new HashMap<>();
        int notFound = 0;
        int invalidCoord = 0;
        for (String hpid : hpids) {
            Optional<EGenItem> opt = eGenApiClient.getHospitalById(hpid);
            if (opt.isEmpty()) { notFound++; continue; }
            HospitalInfo info = HospitalInfo.from(opt.get());
            // 한국 영토 좌표 범위 밖(파싱 실패·일부 누락 포함)은 거리 계산이 왜곡되므로 제외
            if (!isValidKoreanCoordinate(info.lat(), info.lng())) { invalidCoord++; continue; }
            snapshot.put(info.id(), info);
        }

        if (snapshot.isEmpty()) {
            log.warn("응급실 보강 결과 0개 (대상 {}개, 조회 실패 {}, 좌표 누락 {}). 기존 캐시 유지",
                    hpids.size(), notFound, invalidCoord);
            return;
        }
        cache.replaceAll(snapshot);
        log.info("응급의료기관 기본정보 갱신 완료. 병원 {}개 (조회 실패 {}, 좌표 누락 {})",
                snapshot.size(), notFound, invalidCoord);
    }

    /** 한국 영토 좌표 범위 검증. 위도 33~39.5°, 경도 124~132°. */
    private static boolean isValidKoreanCoordinate(double lat, double lng) {
        return lat >= 33.0 && lat <= 39.5 && lng >= 124.0 && lng <= 132.0;
    }
}
