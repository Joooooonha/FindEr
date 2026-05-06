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
import java.util.List;
import java.util.Map;

/** 응급의료기관 기본정보 주기적 폴링. 좌표·연락처는 거의 변하지 않아 24시간 주기로 충분. */
@Component
@RequiredArgsConstructor
public class HospitalInfoScheduler {

    private static final Logger log = LoggerFactory.getLogger(HospitalInfoScheduler.class);

    private final EGenApiClient eGenApiClient;
    private final HospitalInfoCache cache;

    @Scheduled(fixedDelayString = "${hospital.info.refresh-interval-ms}")
    public void refresh() {
        List<EGenItem> items = eGenApiClient.getAllHospitals();
        if (items.isEmpty()) {
            log.warn("응급의료기관 기본정보 갱신 실패 또는 응답 비어있음. 기존 캐시 유지");
            return;
        }

        Map<String, HospitalInfo> snapshot = new HashMap<>();
        for (EGenItem item : items) {
            if (item.getHpid() == null || item.getHpid().isBlank()) continue;
            HospitalInfo info = HospitalInfo.from(item);
            // 한국 영토 좌표 범위 밖(파싱 실패·일부 누락 포함)은 거리 계산이 왜곡되므로 제외
            if (!isValidKoreanCoordinate(info.lat(), info.lng())) continue;
            snapshot.put(info.id(), info);
        }
        cache.replaceAll(snapshot);
        log.info("응급의료기관 기본정보 갱신 완료. 병원 {}개", snapshot.size());
    }

    /** 한국 영토 좌표 범위 검증. 위도 33~39.5°, 경도 124~132°. */
    private static boolean isValidKoreanCoordinate(double lat, double lng) {
        return lat >= 33.0 && lat <= 39.5 && lng >= 124.0 && lng <= 132.0;
    }
}
